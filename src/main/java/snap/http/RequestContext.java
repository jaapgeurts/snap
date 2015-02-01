package snap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.Route;
import snap.Router;
import snap.User;

import com.alibaba.fastjson.JSONObject;

public class RequestContext
{

  final static Logger log = LoggerFactory.getLogger(RequestContext.class);

  private static final String SNAP_AUTHENTICATED_USER = "Snap.AuthenticatedUser";
  private static final String SNAP_CSRF_TOKEN = "Snap.CsrfToken";

  public RequestContext(HttpMethod method, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
  {
    mParams = new HashMap<String, String[]>();
    mParams.putAll(servletRequest.getParameterMap());
    mServletRequest = servletRequest;
    mServletResponse = servletResponse;
    mMethod = method;

    mAuthenticatedUser = (User)mServletRequest.getSession().getAttribute(
        SNAP_AUTHENTICATED_USER);
  }

  public JSONObject getContentAsJson() throws IOException
  {

    BufferedReader reader = mServletRequest.getReader();

    return JSONObject.parseObject(reader.toString());

  }

  public Map<String, String[]> getParams()
  {
    return mParams;
  }

  /**
   * Return variable with name. implicitly returns first value in the list
   * 
   * @param name
   * @return returns null when parameter is not available.
   */
  public String getParam(String name)
  {
    return getParam(name, 0);
  }

  /**
   * Returns a list of all cookies sent with this request Forwards the request
   * to the HttpServletRequest
   * 
   * @return An array of cookies or null of there were no cookies sent
   */
  public Cookie[] getCookies()
  {
    return mServletRequest.getCookies();
  }

  /**
   * Returns a cookie identified by name.
   * 
   * @param name
   *          The name of the cookie
   * @return the cookie of null if there was no cookie by that name
   */
  public Cookie getCookie(String name)
  {
    Cookie[] cookies = mServletRequest.getCookies();
    if (cookies == null)
      return null;

    for (Cookie c : cookies)
    {
      if (c.getName().equals(name))
        return c;
    }
    return null;
  }

  /**
   * Forwards the cookie adding request to HttpServeletResponse
   * 
   * @param cookie
   *          The cookie to the response
   */
  public void addCookie(Cookie cookie)
  {
    mServletResponse.addCookie(cookie);
  }

  /**
   * Return variable with name and in position pos of the list
   * 
   * @param name
   *          the name of the variable
   * @param pos
   *          index of the value in the parameter list
   * @return Returns null when parameter is not available
   */

  public String getParam(String name, int pos)
  {
    String[] list = mParams.get(name);
    if (list != null)
      return list[pos];
    return null;
  }

  public HttpMethod getMethod()
  {
    return mMethod;
  }

  void addParameter(String key, String value)
  {
    String[] values = mParams.get(key);
    if (values == null)
    {
      mParams.put(key, new String[] { value });
    }
    else
    {
      values = new String[values.length + 1];
      values[values.length - 1] = value;
      mParams.put(key, values);
    }
  }

  public void addParameters(Map<String, String[]> params)
  {
    if (params == null)
      return;
    // TODO: check for existing parameters
    mParams.putAll(params);
  }

  public Route getRoute()
  {
    return mRoute;
  }

  public void setRoute(Route route)
  {
    mRoute = route;
  }

  public Router getRouter()
  {
    return mRouter;
  }

  public void setRouter(Router router)
  {
    mRouter = router;
  }

  public HttpServletRequest getRequest()
  {
    return mServletRequest;
  }

  public HttpServletResponse getResponse()
  {
    return mServletResponse;
  }

  public User getAuthenticatedUser()
  {
    return mAuthenticatedUser;
  }

  public HttpRedirect getRedirect(String alias, Object... params)
  {
    return getRouter().redirectForRoute(alias, params);
  }

  public HttpRedirect getRedirect(String alias, Map<String, String> getParams,
      Object... params)
  {
    return getRouter().redirectForRoute(alias, getParams, params);
  }

  /**
   * Puts the authenticated user in the session under the attribute:
   * 'Snap.AuthorizedUser' Use in combination with @LoginRequired. This method
   * should be called only once per session
   * 
   * @param user
   *          The user to store in the session. set to null to remove the user
   *          from the session
   * 
   */
  public void setAuthenticatedUser(User user)
  {
    HttpSession session = mServletRequest.getSession();
    if (user == null)
    {
      session.removeAttribute(SNAP_AUTHENTICATED_USER);
      session.removeAttribute(SNAP_CSRF_TOKEN);
      session.invalidate();
    }
    else
    {
      session.setAttribute(SNAP_AUTHENTICATED_USER, user);
      session.setAttribute(SNAP_CSRF_TOKEN, generateCsrfToken());
    }
    mAuthenticatedUser = user;
  }

  /**
   * Returns the current generated CSRF token
   * 
   * @return
   */
  public String getCsrfToken()
  {
    HttpSession session = mServletRequest.getSession();
    if (session.getAttribute(SNAP_CSRF_TOKEN) != null)
      return (String)session.getAttribute(SNAP_CSRF_TOKEN);
    else
      log.info("User not logged in. Not returning CSRF Token");
    return null;
  }

  public void resetCsrfToken()
  {
    HttpSession session = mServletRequest.getSession();
    if (session.getAttribute(SNAP_CSRF_TOKEN) != null)
      session.setAttribute(SNAP_CSRF_TOKEN, generateCsrfToken());
    else
      log.warn("User not logged in. Not resetting CSRF Token");
  }

  private String generateCsrfToken()
  {
    SecureRandom lSecureRandom = new SecureRandom();

    byte bytes[] = new byte[12];
    lSecureRandom.nextBytes(bytes);
    return byteToHex(bytes);
  }

  private String byteToHex(byte[] array)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < array.length; ++i)
    {
      sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
    }
    return sb.toString();
  }

  private final Map<String, String[]> mParams;

  private HttpServletRequest mServletRequest;
  private HttpServletResponse mServletResponse;

  private HttpMethod mMethod;
  private Route mRoute;
  private User mAuthenticatedUser;
  private Router mRouter;

  public String getRequestURI()
  {
    String uri = mServletRequest.getRequestURI();
    try
    {
      return URLDecoder.decode(uri, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      log.error("JVM Doesn't support UTF8", e);
    }

    // if we can't decode it, then just return it.
    return uri;
  }
}
