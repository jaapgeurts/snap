package snap;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.annotations.AnnotationHandler;
import snap.annotations.LoginRedirect;
import snap.annotations.RouteOptions;
import snap.forms.MissingCsrfTokenException;
import snap.http.HttpMethod;
import snap.http.RequestContext;
import snap.http.RequestResult;

public class ActionRoute implements Route
{
  final Logger log = LoggerFactory.getLogger(ActionRoute.class);

  /**
   * Constructs a new route You must call init() after calling the constructor
   */
  public ActionRoute()
  {
  }

  /**
   * Constructs a new route.
   */
  @Override
  public void init(String contextPath, String alias, String url, String objectMethodPath)
  {

    mAlias = alias;

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
   * Execute this route
   *
   * @param context
   *          The context of the current request
   * @return A request result
   * @throws Throwable
   *           Any error that occurs during execution
   */
  @Override
  public RequestResult handleRoute(RequestContext context) throws Throwable
  {

    // get the method to call (either as interface or reflective name)
    Method actionMethod = getMethod();
    RequestResult result = null;
    if (actionMethod != null)
    {
      Class<?> controllerClass = getController().getClass();
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

      // Check any defined annotations on the controller and execute
      Annotation[] assignedAnnotations = controllerClass.getAnnotations();
      Map<Class<? extends Annotation>, AnnotationHandler> registeredAnnotations = WebApplication.getInstance()
          .getAnnotations();
      for (Annotation annotation : assignedAnnotations)
      {
        AnnotationHandler handler = registeredAnnotations.get(annotation.annotationType());
        if (handler != null)
          handler.execute(getController(), actionMethod, annotation, context);
      }

      // Check any defined annotations on the action method and execute
      assignedAnnotations = actionMethod.getAnnotations();
      for (Annotation annotation : assignedAnnotations)
      {
        AnnotationHandler handler = registeredAnnotations.get(annotation.annotationType());
        if (handler != null)
          handler.execute(getController(), actionMethod, annotation, context);
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
            String message = "ActionRoute specifies controller class but doesn't implement the Controller interface";
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
      String message = "Controller or Method not found for route: " + mAlias + ". Specified: " + mController
          + "::" + mMethodName;
      throw new SnapException(message);
    }
  }

  /**
   * If enabled and authentication fails this route will redirect to the
   * redirect url. see settings:
   *
   * <pre>
   * snap.login.redirect.url
   * </pre>
   *
   * and
   *
   * <pre>
   * snap.login.redirect
   * </pre>
   *
   * Change with @LoginRedirect annotation
   *
   * @return true if redirect is enabled, false otherwise
   */
  @Override
  public boolean isRedirectEnabled()
  {
    LoginRedirect lr = getMethod().getAnnotation(LoginRedirect.class);
    if (lr == null)
      return Settings.redirectEnabled;
    return lr.enabled();

  }

  Object getController()
  {
    // If the app is threadsafe then create a new instance

    if (Settings.threadSafeController)
    {
      try
      {
        return Class.forName(mController).newInstance();
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
        Object mControllerInstance = Class.forName(mController).newInstance();
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

  private boolean validateCsrfToken(RequestContext context)
  {
    // if the user is not logged in do nothing.
    if (context.getAuthenticatedUser() == null)
      return false;

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

  protected Method getMethod()
  {
    if (mMethodRef == null || mMethodRef.get() == null)
    {
      Method m;
      Object controller = getController();
      if (controller == null)
        return null;

      try
      {
        Method[] methods = controller.getClass().getMethods();
        List<Method> methodList = Arrays.stream(methods).filter(x -> x.getName().equals(mMethodName))
            .collect(Collectors.toList());
        if (methodList.size() == 0)
        {
          String message = "ActionRoute " + mAlias + " has no method '" + mMethodName + "()' in controller "
              + mController;
          log.error(message);
          return null;
        }
        else if (methodList.size() > 1)
        {
          String message = "More than one method '" + mMethodName + "' found for route " + mAlias;
          log.error(message);
          return null;
        }
        else
        {
          m = methodList.get(0);
          mMethodRef = new SoftReference<Method>(m);
        }
      }
      catch (SecurityException e)
      {

        log.error("ActionRoute " + mAlias + ". Error accessing method " + mMethodName + " in controller "
            + mController, e);

        return null;
      }
    }
    return mMethodRef.get();
  }

  @Override
  public HttpMethod[] getHttpMethods()
  {
    return mHttpMethods;
  }

  @Override
  public String getLink(String original, Map<String, Object> getParams, Object[] params)
  {
    return original;
  }

  public void setRouteListener(RouteListener listener)
  {
    mRouteListener = listener;
  }

  public RouteListener getRouteListener()
  {
    return mRouteListener;
  }

  @Override
  public String toString()
  {
    String s = "handleRequest()";
    if (mMethodName != null)
      s = mMethodName;
    return "Alias: " + mAlias + ", Endpoint: " + s;
  }

  private RouteListener mRouteListener;

  String mController;
  String mMethodName;
  private String mAlias;

  private boolean mIsControllerInterface;
  // Cached version of the controller
  private SoftReference<Object> mControllerRef = null;
  private HttpMethod[] mHttpMethods;

  SoftReference<Method> mMethodRef = null;

}
