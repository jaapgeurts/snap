package snap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
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
import snap.WebApplication;

import com.alibaba.fastjson.JSONObject;

public class RequestContext
{

  final static Logger log = LoggerFactory.getLogger(RequestContext.class);

  private static final String SNAP_AUTHENTICATED_USER = "Snap.AuthenticatedUser";
  private static final String SNAP_CSRF_TOKEN = "Snap.CsrfToken";
  private static final int SESSION_COOKIE_EXPIRY = -1;

  public static final String SNAP_CSRF_COOKIE_NAME = "csrf_token";

  public RequestContext(HttpMethod method, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
  {
    mServletRequest = servletRequest;
    mServletResponse = servletResponse;
    mMethod = method;

    mAuthenticatedUser = (Long)mServletRequest.getSession().getAttribute(
        SNAP_AUTHENTICATED_USER);
  }

  public JSONObject getContentAsJson() throws IOException
  {

    BufferedReader reader = mServletRequest.getReader();

    return JSONObject.parseObject(reader.toString());

  }

  public Map<String, String[]> getParamsPostGet()
  {
    return mServletRequest.getParameterMap();
  }

  /**
   * Return post or get variable identified by name
   * 
   * @param name
   * @return returns null when parameter is not available.
   */
  public String getParamPostGet(String name)
  {
    return mServletRequest.getParameter(name);
  }

  /**
   * Returns a variable from the decoded URL identified by name
   * 
   * @param name
   * @return
   */
  public String getParamUrl(String name)
  {
    return mUrlParams.get(name);
  }

  // TODO: add method that returns a param by position so that the user
  // is not force to specify variable names

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
    // TODO: put cookies in a map or sort the cookie array and use binary search
    // for speed.
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

  public String getCookieValue(String name)
  {
    Cookie cookie = getCookie(name);
    if (cookie == null)
      return null;

    return cookie.getValue();
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

  public HttpMethod getMethod()
  {
    return mMethod;
  }

  public Route getRoute()
  {
    return mRoute;
  }

  public void setRoute(Route route)
  {
    mRoute = route;
    // set the parameters
    mUrlParams = route.getParameters(getRequestURI());

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
  public void setAuthenticatedUser(Long userid)
  {
    HttpSession session = mServletRequest.getSession();

    if (userid == null)
    {
      session.removeAttribute(SNAP_AUTHENTICATED_USER);
      session.removeAttribute(SNAP_CSRF_TOKEN);
      session.invalidate();
      Cookie cookie = new Cookie(SNAP_CSRF_COOKIE_NAME, getServerCsrfToken());
      // cookie.setDomain("snappix.thaloi.com");
      cookie.setMaxAge(0); // expire now
      cookie.setPath("/");
      addCookie(cookie);
    }
    else
    {
      session.setAttribute(SNAP_AUTHENTICATED_USER, userid);
      session.setAttribute(SNAP_CSRF_TOKEN, generateCsrfToken());
      Cookie cookie = new Cookie(SNAP_CSRF_COOKIE_NAME, getServerCsrfToken());
      // cookie.setDomain("snappix.thaloi.com");
      cookie.setMaxAge(SESSION_COOKIE_EXPIRY);
      cookie.setPath("/");
      addCookie(cookie);
    }
    mAuthenticatedUser = userid;
  }

  public User getAuthenticatedUser()
  {
    if (mAuthenticatedUser == null)
      return null;
    return WebApplication.getInstance().getUser(mAuthenticatedUser);
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
   * Returns the current generated CSRF token
   * 
   * @return
   */
  public String getServerCsrfToken()
  {
    HttpSession session = mServletRequest.getSession();
    if (session.getAttribute(SNAP_CSRF_TOKEN) != null)
      return (String)session.getAttribute(SNAP_CSRF_TOKEN);
    else
      log.debug("User not logged in. Not returning CSRF Token");
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

  private Map<String, String> mUrlParams;

  private HttpServletRequest mServletRequest;
  private HttpServletResponse mServletResponse;

  private HttpMethod mMethod;
  private Route mRoute;
  private Long mAuthenticatedUser;
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
