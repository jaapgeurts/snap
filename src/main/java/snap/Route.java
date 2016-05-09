package snap;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.NameEntry;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.annotations.IgnoreLoginRequired;
import snap.annotations.LoginRedirect;
import snap.annotations.LoginRequired;
import snap.annotations.PermissionRequired;
import snap.annotations.RoleRequired;
import snap.annotations.RouteOptions;
import snap.forms.MissingCsrfTokenException;
import snap.http.HttpMethod;
import snap.http.HttpRedirect;
import snap.http.RequestContext;
import snap.http.RequestResult;

public class Route
{
  final Logger log = LoggerFactory.getLogger(Route.class);

  public Route()
  {
  }

  public Route(String contextPath, String alias, String url)
  {
    mContextPath = contextPath;
    mPath = url;
    mAlias = alias;
    byte[] re = url.getBytes();
    mRegex = new Regex(re, 0, re.length, Option.NONE, UTF8Encoding.INSTANCE);
  }

  public Route(String contextPath, String url, String alias, String objectMethodPath)
  {
    this(contextPath, alias, url);
    if (objectMethodPath.charAt(0) == '.')
      objectMethodPath = Settings.packagePrefix + objectMethodPath;

    String[] parts = objectMethodPath.split("::");
    mController = parts[0];
    if (parts.length == 2)
    {
      // Controller is a method
      mMethodName = parts[1];
      mIsControllerInterface = false;
    }
    else
    {
      // Controller is an interface class
      mMethodName = "handleRequest";
      mIsControllerInterface = true;
    }

    // fetch allowed methods from annotations

    Method m = getMethod();
    if (m != null)
    {
      RouteOptions annotation = m.getAnnotation(RouteOptions.class);
      if (annotation != null)
      {
        mHttpMethods = annotation.methods();
        if (mHttpMethods.length == 0)
          throw new SnapException("You must specify at least 1 HttpMethod in the route options");
      }
      else
        throw new SnapException("RouteOptions annotation not present on controller action");
    }
  }

  /**
   * Returns true if the urlPath matches this route's rule and method
   * 
   * @param method
   * @param urlPath
   * @return
   */
  public boolean match(HttpMethod method, String urlPath)
  {
    boolean found = false;
    for (HttpMethod m : mHttpMethods)
    {
      if (m == method)
      {
        found = true;
        break;
      }
    }
    if (!found)
      return false;

    boolean success = false;
    byte[] p = urlPath.getBytes();
    Matcher m = mRegex.matcher(p);
    success = m.search(0, p.length, Option.DEFAULT) != -1;

    return success;
  }

  /**
   * Returns true if the urlPath matches this route's rule
   * 
   * @param urlPath
   * @return
   */
  public boolean match(String urlPath)
  {
    boolean success = false;
    byte[] p = urlPath.getBytes();
    Matcher m = mRegex.matcher(p);
    success = m.search(0, p.length, Option.DEFAULT) != -1;

    return success;
  }

  public RequestResult handleRoute(RequestContext context) throws Throwable
  {

    // get the method to call (either as interface or reflective name)
    Method actionMethod = getMethod();
    RequestResult result = null;
    if (actionMethod != null)
    {
      Class<?> controllerClass = actionMethod.getDeclaringClass();
      if (mRouteListener != null)
      {
        RequestResult r = mRouteListener.onBeforeRoute(context);
        if (r != null)
          return r;
      }

      // check CSRF
      if (context.getMethod() == HttpMethod.POST || context.getMethod() == HttpMethod.PUT)
      {
        validateCsrfToken(context);
      }

      if ((controllerClass.isAnnotationPresent(LoginRequired.class)
          && !actionMethod.isAnnotationPresent(IgnoreLoginRequired.class))
          || actionMethod.isAnnotationPresent(LoginRequired.class))
      {
        // TODO: think about this, because it requires session and not stateless
        if (context.getAuthenticatedUser() == null)
        {
          boolean isAuthenticated = false;
          // attempt authenticate via Http Basic
          try
          {
            String authToken = decodeAuthentication(context.getHeader("Authorization"));
            if (authToken != null)
            {
              int firstColonPos = authToken.indexOf(':');
              String username = authToken.substring(0, firstColonPos);
              String password = authToken.substring(firstColonPos + 1, authToken.length());

              isAuthenticated = WebApplication.getInstance().authenticateUser(context, username, password);
            }
          }
          catch (Exception e)
          {
            log.error("Error during Basic authentication", e);
          }
          if (!isAuthenticated)
            throw new AuthenticationException("Not allowed to access URL: "
                + context.getRequest().getPathInfo() + ". User not Authenticated");

        }
      }
      if (actionMethod.isAnnotationPresent(RoleRequired.class))
      {
        User user = context.getAuthenticatedUser();
        if (user == null)
          throw new AuthenticationException("Not allowed to access URL: " + context.getRequest().getPathInfo()
              + ". User not Authenticated");

        RoleRequired[] roles = actionMethod.getAnnotationsByType(RoleRequired.class);
        boolean hasRole = Arrays.stream(roles).anyMatch(r -> user.hasRole(r.role()));

        if (!hasRole)
          throw new AuthorizationException(
              "Not allowed to access URL: " + context.getRequest().getPathInfo() + ". User not Authorized");
      }
      if (actionMethod.isAnnotationPresent(PermissionRequired.class))
      {
        User user = context.getAuthenticatedUser();
        if (user == null)
          throw new AuthenticationException("Not allowed to access URL: " + context.getRequest().getPathInfo()
              + ". User not Authenticated");

        // TODO: consider anyMatch vs allMatch
        PermissionRequired[] rights = actionMethod.getAnnotationsByType(PermissionRequired.class);
        boolean hasRight = Arrays.stream(rights).anyMatch(r -> user.hasPermission(r.permission()));
        if (!hasRight)
          throw new AuthorizationException(
              "Not allowed to access URL: " + context.getRequest().getPathInfo() + ". User not Authorized");
      }

      // Execute the actual controller action here.
      try
      {
        Object controller = getController();
        if (mIsControllerInterface)
        {
          if (controller instanceof Controller)
          {
            Controller control = (Controller)controller;
            result = control.handleRequest(context);
          }
          else
          {
            String message = "Route specifies controller class but doesn't implement the Controller interface";
            log.warn(message);
            throw new SnapException(message);
          }
        }
        else
        {
          // check if the method has parameters and try to assign
          Parameter[] parameters = actionMethod.getParameters();
          if (parameters.length < 1) // at least two
            throw new SnapException("Controller method must have at least on parameter: RequestContext");
          if (parameters[0].getType() != RequestContext.class)
            throw new SnapException("First parameter must be a RequestContext");

          // Attempt to assign value to the method arguments
          Object arguments[] = new Object[parameters.length];
          arguments[0] = context;
          if (parameters[0].isNamePresent())
          {
            // Parameter names are supported so check each argument:
            for (int i = 1; i < parameters.length; i++)
            {
              // search all arguments with the same name
              Parameter param = parameters[i];
              String value = context.getParamUrl(param.getName());
              if (value == null)
              {
                log.error("Argument: '" + param.getName() + "' not found in URL param list");
                continue;
              }
              if (param.getType() == String.class)
                arguments[i] = value;
              else if (param.getType() == Integer.class)
                arguments[i] = Integer.valueOf(value);
              else if (param.getType() == Long.class)
                arguments[i] = Long.valueOf(value);
              else
              {
                log.info("You can only assign String, Integer and Long values to controller arguments");
              }
            }
          }
          else
          {
            log.debug("Java 8 is required to support parameter assignments");
          }
          result = (RequestResult)actionMethod.invoke(controller, arguments);
        }
        // controllers should not return NULL
        if (result == null)
          throw new SnapException(
              "Controller " + mController + "::" + mMethodName + " returned null. Expected RequestResult");

      }
      catch (InvocationTargetException e)
      {
        Throwable t = e;
        // unwrap the exception and get the exception thrown by the app
        if (t instanceof InvocationTargetException)
          t = t.getCause();

        throw t;
      }
      catch (IllegalAccessException e)
      {
        String message = "Snap has no invokation access to the controller.";
        log.error(message, e);
        throw new SnapException(message, e);
      }
      catch (IllegalArgumentException e)
      {
        String message = "The method signature of the controller action is not correct.";
        throw new SnapException(message, e);
      }
      catch (ClassCastException e)
      {
        String message = "Instance of RequestResult expected. Found: " + result.getClass().getCanonicalName();
        throw new SnapException(message, e);
      }

      if (mRouteListener != null)
      {
        mRouteListener.onAfterRoute(context);
      }

      return result;
    }
    else

    {
      String message = "Controller or Method not found for route: " + getAlias() + ". Specified: "
          + mController + "::" + mMethodName;
      throw new SnapException(message);
    }
  }

  private String decodeAuthentication(String header)
  {
    if (header == null)
      return null;

    String[] parts = header.trim().split(" ");
    if (parts == null || parts.length != 2)
      return null;

    // only basic supported now
    if (!parts[0].trim().equals("Basic"))
    {
      log.info("Currently only Basic authentication is supported");
      return null;
    }

    String credentials = new String(Base64.getDecoder().decode(parts[1].trim()), StandardCharsets.UTF_8);
    return credentials;

  }

  public String getLink()
  {
    return getLink(null, null);
  }

  public String getLink(Object[] params)
  {
    return getLink(null, params);
  }

  public String getLink(Map<String, String> getParams, Object[] params)
  {
    StringBuilder builder = new StringBuilder();
    java.util.regex.Pattern pat = Pattern.compile("\\(.+?\\)");
    java.util.regex.Matcher m = pat.matcher(mPath);
    int regExLength = mPath.length();
    int start = 0;
    if (mPath.charAt(0) == '^')
      start++;
    int i = 0;
    // TODO: check for missing parameters and report
    while (m.find(start))
    {
      try
      {
        builder.append(mPath.substring(start, m.start()));
        if (i < params.length)
        {
          builder.append(URLEncoder.encode(params[i].toString(), "UTF-8"));
        }
        else
        {
          String message = "Not enough parameters when reversing link: " + mPath;
          log.error(message);
          throw new SnapException(message);
        }
        i++;
        start = m.end();
      }
      catch (UnsupportedEncodingException e)
      {
        log.debug("JVM doesn't support UTF-8", e);
      }
    }
    if (mPath.charAt(regExLength - 1) == '$')
    {
      regExLength--;
      if (start != regExLength)
      {
        builder.append(mPath.substring(start, regExLength));
      }
    }

    // add get params if available
    if (getParams != null)
    {
      builder.append("?");
      for (Map.Entry<String, String> entry : getParams.entrySet())
      {
        try
        {
          builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
          builder.append("=");
          builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
          builder.append("&");
        }
        catch (UnsupportedEncodingException e)
        {
          log.debug("JVM doesn't support UTF-8", e);
        }
      }
      // just make sure
      if (builder.length() >= 1)
        if (builder.charAt(builder.length() - 1) == '&')
          builder.deleteCharAt(builder.length() - 1);

    }

    if (mContextPath == null || "".equals(mContextPath))
      return builder.toString();
    else
      return mContextPath + builder.toString();
  }

  public void setRouteListener(RouteListener listener)
  {
    mRouteListener = listener;
  }

  public RouteListener getRouteListener()
  {
    return mRouteListener;
  }

  public HttpRedirect getRedirect(Object[] params)
  {
    return new HttpRedirect(getLink(params));
  }

  public Map<String, String> getParameters(String path)
  {
    if (path == null || "".equals(path))
      return null;

    HashMap<String, String> map = new HashMap<>();
    byte[] p = path.getBytes();
    Matcher m = mRegex.matcher(p);
    int result = m.search(0, p.length, Option.CAPTURE_GROUP);
    if (result == -1)
      return null;

    // TODO: check the regex group ( if it's of digit class then convert to
    // Long)
    if (result >= 0 && mRegex.numberOfNames() > 0)
    {
      Region region = m.getEagerRegion();
      Iterator<NameEntry> entries = mRegex.namedBackrefIterator();
      while (entries.hasNext())
      {
        NameEntry entry = entries.next();
        String name = new String(entry.name, entry.nameP, entry.nameEnd - entry.nameP);
        int number = entry.getBackRefs()[0];
        int start = region.beg[number];
        int end = region.end[number];
        String value = new String(p, start, end - start, UTF8Encoding.INSTANCE.getCharset());
        map.put(name, value);
      }
    }
    return map;
  }

  public boolean isRedirectEnabled()
  {
    LoginRedirect lr = getMethod().getAnnotation(LoginRedirect.class);
    if (lr == null)
      return Settings.redirectEnabled;
    return lr.enabled();

  }

  public HttpMethod[] getHttpMethods()
  {
    return mHttpMethods;
  }

  public String getAlias()
  {
    return mAlias;
  }

  @Override
  public String toString()
  {
    String s = "handleRequest()";
    if (mMethodName != null)
      s = mMethodName;
    return "Alias: " + mAlias + ", Path: " + mPath + ", Endpoint: " + s;
  }

  private Object getController()
  {
    // If the app is threadsafe then create a new instance

    if (Settings.threadSafeController)
    {
      try
      {
        return (Object)Class.forName(mController).newInstance();
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
      {
        log.error("Can't instantiate controller", e);
        return null;
      }
    }

    // Thread safety is off, don't create new controller instances
    // This means that controllers shouldn't keep state

    if (mControllerRef == null || mControllerRef.get() == null)
    {
      try
      {
        Object mControllerInstance = (Object)Class.forName(mController).newInstance();
        mControllerRef = new SoftReference<Object>(mControllerInstance);
      }
      catch (InstantiationException | ClassNotFoundException | IllegalAccessException e)
      {
        log.error("Can't instantiate controller", e);
        return null;
      }
    }
    return mControllerRef.get();

  }

  private Method getMethod()
  {
    if (mMethodRef == null || mMethodRef.get() == null)
    {
      Method m;
      Object controller = getController();
      if (controller == null)
        return null;

      try
      {
        m = controller.getClass().getMethod(mMethodName, RequestContext.class);
        mMethodRef = new SoftReference<Method>(m);
      }
      catch (NoSuchMethodException | SecurityException e)
      {
        Method[] methods = controller.getClass().getMethods();
        Optional<Method> result = Arrays.stream(methods).filter(x -> x.getName().equals(mMethodName))
            .findFirst();
        if (result.isPresent())
          log.error("Route " + getAlias() + " has method " + mMethodName + " in controller " + mController
              + " but has wrong signature. Expected: " + mMethodName + "(RequestContext); found: "
              + result.get().toGenericString(), e);
        else
          log.error("Route " + getAlias() + " has no method " + mMethodName + " in controller " + mController,
              e);

        return null;
      }
    }
    return mMethodRef.get();
  }

  private boolean validateCsrfToken(RequestContext context)
  {
    // if the user is not logged in do nothing.
    if (context.getAuthenticatedUser() == null)
    {
      log.debug("User not logged in. Not checking Csrf token");
      return false;
    }

    String token = context.getParamPostGet("csrf_token");
    if (token == null)
    {
      // attempt to get the token from the HTTP header X-CSRFToken
      token = context.getRequest().getHeader("X-CSRFToken");
      if (token == null)
      {
        Cookie cookie = context.getCookie(RequestContext.SNAP_CSRF_COOKIE_NAME);
        token = cookie.getValue();
        if (token == null)
        {
          String message = "Csrf Token not found. Did you forget to include it with @csrf_token()";
          log.warn("Possible hacking attempt! " + message);
          throw new MissingCsrfTokenException(
              "Token not found in Cookie, X-CSRFToken header or Get/Post parameters.");
        }
      }
    }

    if (!token.equals(context.getServerCsrfToken()))
    {
      String message = "The submitted csrf token value did not match the expected value.";
      log.warn("Possible hacking attempt! " + message);
      throw new InvalidCsrfTokenException(message);
    }

    return true;
  }

  protected String mPath;
  protected String mContextPath;
  private String mAlias;
  private String mController;
  protected HttpMethod[] mHttpMethods;
  private String mMethodName;
  private RouteListener mRouteListener;

  private boolean mIsControllerInterface;
  protected Regex mRegex;

  // Cached version of the controller
  private SoftReference<Object> mControllerRef = null;
  private SoftReference<Method> mMethodRef = null;

}
