package com.proficiosoftware.snap;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.NameEntry;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proficiosoftware.snap.annotations.HttpGet;
import com.proficiosoftware.snap.annotations.HttpPost;
import com.proficiosoftware.snap.annotations.LoginRequired;
import com.proficiosoftware.snap.http.HttpRequest;
import com.proficiosoftware.snap.http.HttpResponse;
import com.proficiosoftware.snap.views.ErrorView;
import com.proficiosoftware.snap.views.View;

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

  public Route(String contextPath, String method, String path, String alias,
      String objectMethodPath)
  {
    this(contextPath, alias, path);
    // TODO: check * route
    mHttpMethod = method;
    String[] parts = objectMethodPath.split("::");
    mController = parts[0];
    mMethodName = parts[1];
  }

  // TODO: store parameters immediately
  public boolean match(String method, String path)
  {
    if (mHttpMethod != null)
    {
      if (mHttpMethod.charAt(0) != '*' && !method.equalsIgnoreCase(mHttpMethod))
        return false;
    }
    boolean success = false;
    byte[] p = path.getBytes();
    Matcher m = mRegex.matcher(p);
    success = m.search(0, p.length, Option.DEFAULT) != -1;

    return success;
  }

  public View handleRoute(HttpRequest httpRequest, HttpResponse httpResponse)
      throws UnauthorizedAccessException, HttpMethodException
  {
    Method actionMethod = getMethod();
    View view = null;
    if (actionMethod != null)
    {
      boolean methodPresent = false;

      if (actionMethod.isAnnotationPresent(LoginRequired.class))
      {
        if (httpRequest.getAuthorizedUser() == null)
          throw new UnauthorizedAccessException("Not allowed to access URL: "
              + httpRequest.getRequest().getPathInfo());
      }
      if (actionMethod.isAnnotationPresent(HttpPost.class))
      {
        methodPresent = true;
        if (!HttpRequest.HTTP_POST.equals(httpRequest.getMethod()))
          throw new HttpMethodException(
              "Action method doesn't accept Http POST method. Annotate your method with '@HttpPost' or remove all annotations");
      }
      if (actionMethod.isAnnotationPresent(HttpGet.class))
      {
        methodPresent = true;
        if (!HttpRequest.HTTP_GET.equals(httpRequest.getMethod()))
          throw new HttpMethodException(
              "Action method doesn't accept Http GET method. Annotate your method with '@HttpPost' or remove all annotations");
      }
      if (!methodPresent)
        throw new HttpMethodException("You must specifiy one or more Http Methods for this controller action with @HttpPost or @HttpGet");

      try
      {
        view = (View)actionMethod.invoke(getController(), httpRequest,
            httpResponse);
        return view;
      }
      catch (InvocationTargetException e)
      {
        Throwable t = e;
        if (t instanceof InvocationTargetException)
          t = t.getCause();
        // TODO: wording
        String message = "Error happened during controller action.";
        log.error(message, t);
        view = new ErrorView(message, t);
        return view;

      }
      catch (IllegalAccessException e)
      {
        String message = "Snap has no invokation access to the controller.";
        log.error(message, e);
        view = new ErrorView(message, e);
        return view;
      }
      catch (IllegalArgumentException e)
      {
        String message = "The method signature of the controller action is not correct.";
        log.error(message, e);
        view = new ErrorView(message, e);
        return view;
      }
      catch (ClassCastException cce)
      {
        String message = "Instance of view expected. Found: "
            + view.getClass().getCanonicalName();
        log.error(message, cce);
        view = new ErrorView(message, cce);
        return view;
      }
    }
    else
    {
      String message = "Controller or Method not found for route: "
          + getAlias() + ". Specified: " + mController + "::" + mMethodName;
      view = new ErrorView(message);
      log.warn(message);
      return view;
    }
  }

  public String getLink(Object[] params)
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
        builder.append(params[i].toString());
        i++;
        start = m.end();
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
        String message = "Not enough parameters when reversing link: " + mPath;
        log.debug(message, e);
        throw new RuntimeException(message, e);
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
            HttpRequest.class, HttpResponse.class);
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

  public String getHttpMethod()
  {
    return mHttpMethod;
  }

  public String getAlias()
  {
    return mAlias;
  }

  protected String mPath;
  protected String mContextPath;
  private String mAlias;
  private String mController;
  private String mHttpMethod;
  private String mMethodName;

  protected Regex mRegex;

  // Cached version of the controller
  private SoftReference<Object> mControllerRef = null;
  private SoftReference<Method> mMethodRef = null;

}
