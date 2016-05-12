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

/**
 * This class represents information about the current HTTP request. Most
 * applications will use this object to get access to the HTTP parameters, URL
 * parameters, Cookies and the current Authenticated User. It contains the
 * original HttpServletRequest and HttpServletResponse objects.
 * 
 * @author Jaap Geurts
 *
 */
public class RequestContext
{

  final static Logger log = LoggerFactory.getLogger(RequestContext.class);

  private static final String SNAP_AUTHENTICATED_USER = "Snap.AuthenticatedUser";
  private static final String SNAP_CSRF_TOKEN = "Snap.CsrfToken";
  private static final int SESSION_COOKIE_EXPIRY = -1;

  public static final String SNAP_CSRF_COOKIE_NAME = "csrf_token";

  /**
   * Construct a RequestContext. You should never need to construct this object,
   * it is constructed by the Snap framework.
   * 
   * @param method
   *          The HTTP Method of the request.
   * @param servletRequest
   *          The original servlet request
   * @param servletResponse
   *          The original servlet response
   */
  public RequestContext(HttpMethod method, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
  {
    mServletRequest = servletRequest;
    mServletResponse = servletResponse;
    mMethod = method;

    mSession = mServletRequest.getSession(false);

    if (mSession != null)
    {
      mAuthenticatedUser = (Long)mSession.getAttribute(SNAP_AUTHENTICATED_USER);
    }
  }

  /**
   * If the content sent to the server was in Json Format you can use this
   * method to get a parsed Json object
   * 
   * @return The Json Object for the data
   * @throws IOException
   */
  public JSONObject getContentAsJson() throws IOException
  {

    BufferedReader reader = mServletRequest.getReader();

    return JSONObject.parseObject(reader.toString());

  }

  /**
   * Returns a Map of all parameters. GET and POST parameters are merged into
   * the same dictionary.
   * 
   * WARNING: if you have a post parameter with the name name as a GET parameter
   * you will only see one of them. This is a limitation of the Servlet spec
   * 
   * @return
   */
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
   * Returns a variable from the decoded URL identified by name. These are values
   * that appear regex expression in the Route.
   * 
   * @param name
   * @return Null if the parameter is not available
   */
  public String getParamUrl(String name)
  {
    return mUrlParams.get(name);
  }

  /**
   * Return all params and the values that were extracted from the URL
   * 
   * @return
   */
  public Map<String, String> getParamUrls()
  {
    return mUrlParams;

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

  /**
   * Get the string of the cookie by name
   * 
   * @param name
   *          The name of the cookie
   * @return The value or null or null if no cookie exists by that name
   */
  public String getCookieValue(String name)
  {
    Cookie cookie = getCookie(name);
    if (cookie == null)
      return null;

    return cookie.getValue();
  }

  /**
   * Adds a cookie to HttpServeletResponse
   * 
   * @param cookie
   *          The cookie to add to the response
   */
  public void addCookie(Cookie cookie)
  {
    mServletResponse.addCookie(cookie);
  }

  /**
   * Get the value for an HTTP Header.
   * 
   * @param header
   *          the name of the header
   * @return The value or null if header doesn't exist
   */
  public String getHeader(String header)
  {
    return mServletRequest.getHeader(header);
  }
  
  /**
   * Returns the HTTP referer header, if available, else NULL
   * @return
   */
  public String getReferrerURL()
  {
    return mServletRequest.getHeader("referer");
  }

  /**
   * Gets the HTTP Method by which this request was called.
   * 
   * @return The method
   */
  public HttpMethod getMethod()
  {
    return mMethod;
  }

  /**
   * Gets the route object that led to this request.
   * 
   * @return
   */
  public Route getRoute()
  {
    return mRoute;
  }

  /**
   * Used by the framework. Sets the route object for this request
   * 
   * @param route
   */
  public void setRoute(Route route)
  {
    mRoute = route;
    // set the parameters
    mUrlParams = route.getParameters(getRequestURI());

  }

  /**
   * Get the Framework Router.
   * 
   * @return the Router
   */
  public Router getRouter()
  {
    return mRouter;
  }

  /**
   * Sets the Framework Router
   * 
   * @param router
   */
  public void setRouter(Router router)
  {
    mRouter = router;
  }

  /**
   * Gets the Original Servlet request
   * 
   * @return
   */
  public HttpServletRequest getRequest()
  {
    return mServletRequest;
  }

  /**
   * Get the original servlet response
   * 
   * @return
   */
  public HttpServletResponse getResponse()
  {
    return mServletResponse;
  }

  /**
   * Puts the authenticated user in the session under the attribute:
   * 'Snap.AuthorizedUser' Use in combination with @LoginRequired. This method
   * should be called only once per session
   * 
   * @param userid
   *          The userid to store in the session. set to null to remove the user
   *          from the session
   * 
   */
  public void setAuthenticatedUser(Long userid)
  {
    mAuthenticatedUser = userid;
    if (mSession != null)
    {
      if (userid == null)
      {
        mSession.removeAttribute(SNAP_AUTHENTICATED_USER);
        mSession.removeAttribute(SNAP_CSRF_TOKEN);
        mSession.invalidate();
        Cookie cookie = new Cookie(SNAP_CSRF_COOKIE_NAME, getServerCsrfToken());
        // cookie.setDomain("snappix.thaloi.com");
        cookie.setMaxAge(0); // expire now
        cookie.setPath("/");
        addCookie(cookie);
      }
      else
      {
        mSession.setAttribute(SNAP_AUTHENTICATED_USER, userid);
        mSession.setAttribute(SNAP_CSRF_TOKEN, generateCsrfToken());
        Cookie cookie = new Cookie(SNAP_CSRF_COOKIE_NAME, getServerCsrfToken());
        // cookie.setDomain("snappix.thaloi.com");
        cookie.setMaxAge(SESSION_COOKIE_EXPIRY);
        cookie.setPath("/");
        addCookie(cookie);
      }

    }
  }

  /**
   * Gets the authenticed User if any.
   * 
   * @return The user or null if no user is authenticated
   */
  public User getAuthenticatedUser()
  {
    if (mAuthenticatedUser == null)
      return null;
    return WebApplication.getInstance().getUser(mAuthenticatedUser);
  }

  /**
   * Start a new session for this browser.
   */
  public void startSession()
  {
    mServletRequest.getSession(true);
  }

  /**
   * Get a Redirect to the same route that this request was routed to.
   * 
   * @param params
   *          The params to replace in the route URL
   * @return the redirect object
   */
  public HttpRedirect getRedirectSelf(Object... params)
  {
    return getRoute().getRedirect(params);
  }

  /**
   * Gets a Redirect object to the URL router using the alias name.
   * 
   * @param alias
   *          the route identified by alias name
   * @param params
   *          the parameters to replace in the URL
   * @return the redirect object
   */
  public HttpRedirect getRedirect(String alias, Object... params)
  {
    return getRouter().redirectForRoute(alias, params);
  }

  /**
   * Same as getRedirect(String alias, Object... params) but also appends
   * getParams as parameters to the end of the URL in the form of
   * ?K1=V1&amp;K2=V2
   * 
   * @param alias
   *          the route identified by alias name
   * @param getParams
   *          The dictionary of GET params
   * @param params
   *          the parameters to replace in the URL
   * @return the redirect object
   * 
   */
  public HttpRedirect getRedirect(String alias, Map<String, String> getParams, Object... params)
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

  /**
   * Resets the current CsrfToken in use
   */
  public void resetCsrfToken()
  {
    HttpSession session = mServletRequest.getSession();
    if (session.getAttribute(SNAP_CSRF_TOKEN) != null)
      session.setAttribute(SNAP_CSRF_TOKEN, generateCsrfToken());
    else
      log.warn("User not logged in. Not resetting CSRF Token");
  }

  /**
   * Generate a CsrfToken
   * 
   * @return the CsrfToken
   */
  private String generateCsrfToken()
  {
    SecureRandom lSecureRandom = new SecureRandom();

    byte bytes[] = new byte[12];
    lSecureRandom.nextBytes(bytes);
    return byteToHex(bytes);
  }

  /**
   * Convert bytes to a string representation
   * 
   * @param array
   * @return the String
   */
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

  private HttpSession mSession;

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
