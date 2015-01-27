package snap;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.NameEntry;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.annotations.HttpGet;
import snap.annotations.HttpPost;
import snap.annotations.LoginRequired;
import snap.annotations.PermissionRequired;
import snap.annotations.RoleRequired;
import snap.http.HttpMethod;
import snap.http.RequestContext;
import snap.http.RequestResult;

public class Route
{
  final Logger log = LoggerFactory.getLogger(Route.class);

  public Route()
  {
  }

  public Route(String contextPath, String alias, String path)
  {
    mContextPath = contextPath;
    mPath = path;
    mAlias = alias;
    byte[] re = path.getBytes();
    mRegex = new Regex(re, 0, re.length, Option.NONE, UTF8Encoding.INSTANCE);
  }

  public Route(String contextPath, HttpMethod method, String path,
      String alias, String objectMethodPath)
  {
    this(contextPath, alias, path);
    mHttpMethod = method;
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

  }

  // TODO: store parameters immediately
  public boolean match(HttpMethod method, String path)
  {
    if (mHttpMethod != method)
      return false;

    boolean success = false;
    byte[] p = path.getBytes();
    Matcher m = mRegex.matcher(p);
    success = m.search(0, p.length, Option.DEFAULT) != -1;

    return success;
  }

  public RequestResult handleRoute(RequestContext context) throws Throwable
  {
    Method actionMethod = getMethod();
    RequestResult result = null;
    if (actionMethod != null)
    {
      if (mRouteListener != null)
      {
        RequestResult r = mRouteListener.onBeforeRoute(context);
        if (r != null)
          return r;
      }
      // Check all annotations
      switch(context.getMethod())
      {
        case GET:
          if (!actionMethod.isAnnotationPresent(HttpGet.class))
            throw new HttpMethodException(
                "Action method "
                    + actionMethod.getName()
                    + " doesn't accept Http GET method. Annotate your method with '@HttGet'");
          break;
        case POST:
          if (!actionMethod.isAnnotationPresent(HttpPost.class))
            throw new HttpMethodException(
                "Action method  "
                    + actionMethod.getName()
                    + " doesn't accept Http POST method. Annotate your method with '@HttpPost'");
          break;
        default:
          // do nothing, there is nothing to check here.
          break;
      }
      if (actionMethod.isAnnotationPresent(LoginRequired.class))
      {
        // TODO: think about this, because it requires session and not stateless
        if (context.getAuthenticatedUser() == null)
          throw new AuthenticationException("Not allowed to access URL: "
              + context.getRequest().getPathInfo() + ". User not Authenticated");
      }
      if (actionMethod.isAnnotationPresent(RoleRequired.class))
      {
        User user = context.getAuthenticatedUser();
        if (user == null)
          throw new AuthenticationException("Not allowed to access URL: "
              + context.getRequest().getPathInfo() + ". User not Authenticated");

        RoleRequired[] roles = actionMethod
            .getAnnotationsByType(RoleRequired.class);
        boolean hasRole = Arrays.stream(roles).anyMatch(
            r -> user.hasRole(r.role()));

        if (!hasRole)
          throw new AuthorizationException("Not allowed to access URL: "
              + context.getRequest().getPathInfo() + ". User not Authorized");
      }
      if (actionMethod.isAnnotationPresent(PermissionRequired.class))
      {
        User user = context.getAuthenticatedUser();
        if (user == null)
          throw new AuthenticationException("Not allowed to access URL: "
              + context.getRequest().getPathInfo() + ". User not Authenticated");

        // TODO: consider anyMatch vs allMatch
        PermissionRequired[] rights = actionMethod
            .getAnnotationsByType(PermissionRequired.class);
        boolean hasRight = Arrays.stream(rights).anyMatch(
            r -> user.hasPermission(r.permission()));
        if (!hasRight)
          throw new AuthorizationException("Not allowed to access URL: "
              + context.getRequest().getPathInfo() + ". User not Authorized");
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
          result = (RequestResult)actionMethod.invoke(controller, context);
        }
        // controllers should not return NULL
        if (result == null)
          throw new SnapException("Controller " + mController + "::"
              + mMethodName + " returned null. Expected RequestResult");

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
        String message = "Instance of RequestResult expected. Found: "
            + result.getClass().getCanonicalName();
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
      String message = "Controller or Method not found for route: "
          + getAlias() + ". Specified: " + mController + "::" + mMethodName;
      throw new SnapException(message);
    }
  }

  public String getLink()
  {
    return getLink(null, null);
  }

  public String getLink(Object[] params)
  {
    return getLink(null, params);
  }

  public void setRouteListener(RouteListener listener)
  {
    mRouteListener = listener;
  }

  public RouteListener getRouteListener()
  {
    return mRouteListener;
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
          String message = "Not enough parameters when reversing link: "
              + mPath;
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

  public Map<String, String[]> getParameters(String path)
  {
    if (path == null || "".equals(path))
      return null;

    HashMap<String, String[]> map = new HashMap<String, String[]>();
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
        String name = new String(entry.name, entry.nameP, entry.nameEnd
            - entry.nameP);
        int number = entry.getBackRefs()[0];
        int start = region.beg[number];
        int end = region.end[number];
        String value = new String(p, start, end - start,
            UTF8Encoding.INSTANCE.getCharset());
        map.put(name, new String[] { value });
      }
    }
    return map;
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
      catch (InstantiationException | IllegalAccessException
          | ClassNotFoundException e)
      {
        log.error("Can't instantiate controller", e);
        return null;
      }
    }

    if (mControllerRef == null || mControllerRef.get() == null)
    {
      try
      {
        Object mControllerInstance = (Object)Class.forName(mController)
            .newInstance();
        mControllerRef = new SoftReference<Object>(mControllerInstance);
      }
      catch (InstantiationException | ClassNotFoundException
          | IllegalAccessException e)
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
      try
      {
        Object controller = getController();
        if (controller == null)
          return null;
        m = getController().getClass().getMethod(mMethodName,
            RequestContext.class);
        mMethodRef = new SoftReference<Method>(m);
      }
      catch (NoSuchMethodException | SecurityException e)
      {
        log.error("Can't get method for name: " + mMethodName, e);
        return null;
      }
    }
    return mMethodRef.get();
  }

  public HttpMethod getHttpMethod()
  {
    return mHttpMethod;
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

  protected String mPath;
  protected String mContextPath;
  private String mAlias;
  private String mController;
  protected HttpMethod mHttpMethod;
  private String mMethodName;
  private RouteListener mRouteListener;

  private boolean mIsControllerInterface;
  protected Regex mRegex;

  // Cached version of the controller
  private SoftReference<Object> mControllerRef = null;
  private SoftReference<Method> mMethodRef = null;

}
