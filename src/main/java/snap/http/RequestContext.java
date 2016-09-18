package snap.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.Route;
import snap.RouteMatcher;
import snap.Router;
import snap.Settings;
import snap.User;
import snap.WebApplication;

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
  private static final int CSRF_COOKIE_EXPIRY = -1;

  public static final String SNAP_CSRF_COOKIE_NAME = "csrf_token";

  public static final String SNAP_USER_LANGUAGE = "Snap.UserLanguageTag";

  // set to 10 years
  private static final int LANGUAGE_COOKIE_EXPIRY = 10 * 365 * 24 * 60 * 60;

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
   * Returns a Map of all parameters. GET and POST parameters are merged into
   * the same dictionary.
   *
   * WARNING: if you have a post parameter with the name name as a GET parameter
   * you will only see one of them. This is a limitation of the Servlet spec
   *
   * @return the parameters a Map
   */
  public Map<String, String[]> getParamsPostGet()
  {
    return mServletRequest.getParameterMap();
  }

  /**
   * Return post or get variable identified by name
   *
   * @param name
   *          the parameter to find
   * @return returns null when parameter is not available.
   */
  public String getParamPostGet(String name)
  {
    return mServletRequest.getParameter(name);
  }

  /**
   * Returns a variable from the decoded URL identified by name. These are
   * values that appear regex expression in the Route.
   *
   * @param name
   *          the param to find.
   * @return Null if the parameter is not available
   */
  public String getParamUrl(String name)
  {
    return mUrlParams.get(name);
  }

  /**
   * Return all params and the values that were extracted from the URL
   *
   * @return all params name/value in the URL
   */
  public Map<String, String> getParamUrls()
  {
    return mUrlParams;

  }

  // TODO: add method that returns a param by position so that the user
  // is not forced to specify variable names

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
   * Removes the cookie from the user browser. This is the same as calling
   * cookie.setMaxAge(0) and adding the cookie to the response
   *
   * @param cookie
   *          The cookie to remove from the client
   */
  public void removeCookie(Cookie cookie)
  {
    cookie.setMaxAge(0);
    cookie.setValue(null);
    cookie.setPath("/");
    String domain = Settings.get("snap.session.cookie.domain", Router.getInstance().siteUri().getHost());
    if (!"localhost".equals(domain) && !".localhost".equals(domain))
      cookie.setDomain(domain);
    addCookie(cookie);
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
   *
   * @return The referer value
   */
  public String getReferrerURL()
  {
    return mServletRequest.getHeader("referer");
  }

  /**
   * Returns the decoded url path of this url. call getQuery to get the query
   * string
   *
   * @return the decoded url path
   */
  public String getPath()
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

  /**
   * Returns the decoded query string
   *
   * @return the decoded query
   */
  public String getQuery()
  {
    String query = mServletRequest.getQueryString();
    if (query == null)
      return null;

    try
    {
      return URLDecoder.decode(query, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      log.error("JVM Doesn't support UTF8", e);
    }
    // if we can't decode it, then just return it.
    return query;
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
   * Call this method if you want to forward this request to the next rule in
   * the list
   *
   * @return the result from the next route
   * @throws Throwable
   *           Any error thrown
   */
  public RequestResult chainNextRoute() throws Throwable
  {
    RouteMatcher matcher = Router.getInstance().findNextRouteMatcherForPath(getMethod(), getPath(),
                                                                            getRouteMatcher());
    setRouteMatcher(matcher);
    return matcher.handleRoute(this);
  }

  /**
   * Gets the route object that led to this request.
   *
   * @return the route for this request
   */
  public Route getRoute()
  {
    return mRoute.getRoute();
  }

  /**
   * Gets the route matcher object that led to this request.
   *
   * @return the route for this request
   */
  public RouteMatcher getRouteMatcher()
  {
    return mRoute;
  }

  /**
   * Used by the framework. Sets the route object for this request
   *
   * @param routeMatcher
   *          the route matcher for this request. Set by the framework.
   */
  public void setRouteMatcher(RouteMatcher routeMatcher)
  {
    mRoute = routeMatcher;
    // set the parameters
    mUrlParams = routeMatcher.getParameters(getPath());

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
   *          the router in use
   */
  public void setRouter(Router router)
  {
    mRouter = router;
  }

  /**
   * Gets the Original Servlet request
   *
   * @return the original servlet request
   */
  public HttpServletRequest getRequest()
  {
    return mServletRequest;
  }

  /**
   * Get the original servlet response
   *
   * @return the original servlet response
   */
  public HttpServletResponse getResponse()
  {
    return mServletResponse;
  }

  /**
   * For internal use. If the language should be persisted for next requests
   *
   * @return whether to persist the language or not
   */
  public boolean isPersistLanguage()
  {
    return mPersistLanguage;
  }

  /**
   * Puts the authenticated user in the session under the attribute:
   * 'Snap.AuthorizedUser' Use in combination with @LoginRequired. This method
   * should be called only once per session
   *
   * @param userid
   *          The userid to store in the session. Set to null to remove the user
   *          from the session
   *
   */
  public void setAuthenticatedUser(Long userid)
  {
    mAuthenticatedUser = userid;
    if (mSession == null)
    {
      log.warn("Attempt to set authenticated user without an active session");
      return;
    }

    if (userid == null)
    {
      mSession.removeAttribute(SNAP_AUTHENTICATED_USER);
      mSession.removeAttribute(SNAP_CSRF_TOKEN);
      mSession.invalidate();
      Cookie cookie = new Cookie(SNAP_CSRF_COOKIE_NAME, getServerCsrfToken());
      removeCookie(cookie);
    }
    else
    {
      mSession.setAttribute(SNAP_AUTHENTICATED_USER, userid);
      mSession.setAttribute(SNAP_CSRF_TOKEN, generateCsrfToken());
      Cookie cookie = new Cookie(SNAP_CSRF_COOKIE_NAME, getServerCsrfToken());
      cookie.setMaxAge(CSRF_COOKIE_EXPIRY);
      cookie.setPath("/");
      String domain = Settings.get("snap.session.cookie.domain", mRouter.siteUri().getHost());
      if (!"localhost".equals(domain) && !".localhost".equals(domain))
        cookie.setDomain(domain);
      addCookie(cookie);
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
   * Set the language for this request. If you set persist to true then you only
   * have to set this once. The language setting will be stored in the user
   * session or a user cookie depending on the 'snap.site.localemode' setting.
   * If you set persist to false the setting is not saved. If
   * snap.site.localemode = cookie or session, then the cookie or session state
   * for the language is managed by the framework. If the snap.site.localemode =
   * subdomain then when you change the language a redirect will be forced. If
   * snap.site.localemode = custom then you can specify behaviour yourself
   *
   * @param language
   *          Use a standard Locale.getLanguage() value or set to null to remove
   *          and switch back to the default locale
   * @param persist
   *          <ul>
   *          <li>True - Save the setting to cookie, session or database,</li>
   *          <li>False - just save the setting in the current RequestContext
   *          </li>
   *          </ul>
   *
   */
  public void setLanguage(String language, boolean persist)
  {
    mLanguage = language;

    mPersistLanguage = persist;

    if (!persist)
      return;

    switch(Settings.localeMode)
    {
      case SESSION:
        if (language == null)
          mSession.removeAttribute(SNAP_USER_LANGUAGE);
        else
          mSession.setAttribute(SNAP_USER_LANGUAGE, language);
        break;
      case COOKIE:
        Cookie cookie = new Cookie(SNAP_USER_LANGUAGE, "");
        cookie.setPath("/");
        if (language == null)
        {
          removeCookie(cookie);
        }
        else
        {
          cookie.setValue(language);
          cookie.setMaxAge(LANGUAGE_COOKIE_EXPIRY);
          addCookie(cookie);
        }
        break;
      case SUBDOMAIN:
        // this functionality lives in Dispatcher.java because changing the
        // subdomain requires
        // the framework to send a redirect URL. We can't do that from here.
        break;
      default:
        log.warn("RequestContext::setLanguage(localeMode) " + Settings.localeMode.toString()
            + " not implemented.");
        break;
    }
  }

  /**
   * Returns the language for this request. If the language was previously set
   * with setLanguage(language,persist) then that value will be returned. If
   * persist was true then the language will be saved between requests and
   * returned.
   *
   * @return The language in BCP47 notation or null if no language was set and
   *         no Accept-Language was sent
   */
  public String getLanguage()
  {
    if (mLanguage != null)
      return mLanguage;

    switch(Settings.localeMode)
    {
      case SESSION:
        mLanguage = (String)mSession.getAttribute(SNAP_USER_LANGUAGE);
        break;
      case COOKIE:
        Cookie cookie = getCookie(SNAP_USER_LANGUAGE);
        if (cookie != null)
          mLanguage = cookie.getValue();
        break;
      case SUBDOMAIN:
        String host = getRequest().getServerName();
        // host could be an IP address. ignore for now
        String[] hostParts = host.split("\\.");
        Locale.Builder builder = new Locale.Builder();
        try
        {
          builder.setLanguageTag(hostParts[0]);
          mLanguage = hostParts[0];
        }
        catch (IllformedLocaleException ile)
        {
        }
        break;
      case CUSTOM:
        mLanguage = WebApplication.getInstance().retrieveLanguage(this);
        break;
      default:
        log.warn("RequestContext::getLanguage() " + Settings.localeMode.toString() + " not implemented.");
        break;
    }
    return mLanguage;
  }

  /**
   * Returns the Locale for this request or null if no language is set. This is
   * a convenience function which creates a local based on the value of
   * getLanguage()
   *
   * @return The locale or null if no language is set
   */
  public Locale getLocale()
  {
    if (mLocale != null)
      return mLocale;

    String language = getLanguage();
    if (language != null)
    {
      try
      {
        return new Locale.Builder().setLanguageTag(language).build();
      }
      catch (IllformedLocaleException ile)
      {
        log.error("Language: " + language + " not recognized", ile);
      }
    }
    return null;
  }

  /**
   * Start a new session for this browser. Calling multiple times has no effect.
   */
  public void startSession()
  {
    mSession = mServletRequest.getSession();
  }

  /**
   * Ends the session for a user. Calling multiple times has no effect.
   */
  public void endSession()
  {
    HttpSession s = mServletRequest.getSession(false);
    if (s != null)
      s.invalidate();
  }

  /**
   * Get a Redirect to the same route that this request was routed to, will send
   * a default /TEMPORARY_ALLOW_CHANGE=SC_FOUND
   *
   * @param params
   *          The params to replace in the route URL
   * @return the redirect object
   */
  public HttpRedirect getRedirectSelf(Object... params)
  {
    return getRouteMatcher().getRedirect(params, RedirectType.TEMPORARY_ALLOW_CHANGE);
  }

  /**
   * Get a Redirect to the same route that this request was routed to.
   *
   * @param type
   *          Redirect type to send back to the client
   * @param params
   *          The params to replace in the route URL
   * @return the redirect object
   */
  public HttpRedirect getRedirectSelf(RedirectType type, Object... params)
  {
    return getRouteMatcher().getRedirect(params, type);
  }

  /**
   * Gets a Redirect object to the URL router using the alias name.
   *
   * @param alias
   *          the route identified by alias name
   *
   * @param params
   *          the parameters to replace in the URL
   * @return the redirect object
   */
  public HttpRedirect getRedirect(String alias, Object... params)
  {
    return getRouter().redirectForRoute(alias, RedirectType.TEMPORARY_ALLOW_CHANGE, params);
  }

  /**
   * Gets a Redirect object to the URL router using the alias name.
   *
   * @param alias
   *          the route identified by alias name
   * @param type
   *          Redirect type to send back to the client
   * @param params
   *          the parameters to replace in the URL
   * @return the redirect object
   */
  public HttpRedirect getRedirect(String alias, RedirectType type, Object... params)
  {
    return getRouter().redirectForRoute(alias, type, params);
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
  public HttpRedirect getRedirect(String alias, Map<String, Object> getParams, Object... params)
  {
    return getRouter().redirectForRoute(alias, RedirectType.TEMPORARY_ALLOW_CHANGE, getParams, params);
  }

  /**
   * Same as getRedirect(String alias, Object... params) but also appends
   * getParams as parameters to the end of the URL in the form of
   * ?K1=V1&amp;K2=V2
   *
   * @param alias
   *          the route identified by alias name
   * @param type
   *          Redirect type to send back to the client
   * @param getParams
   *          The dictionary of GET params
   * @param params
   *          the parameters to replace in the URL
   * @return the redirect object
   *
   */
  public HttpRedirect getRedirect(String alias, RedirectType type, Map<String, Object> getParams,
                                  Object... params)
  {
    return getRouter().redirectForRoute(alias, type, getParams, params);
  }

  /**
   * Same as getRedirect(String alias) but also appends getParams as parameters
   * to the end of the URL in the form of ?K1=V1&amp;K2=V2
   *
   * @param alias
   *          The route identified by the alias name
   * @param type
   *          Redirect type to send back to the client
   * @param getParams
   *          The dictionary of the get params
   * @return the redirect object
   */
  public HttpRedirect getRedirect(String alias, RedirectType type, Map<String, Object> getParams)
  {
    return getRouter().redirectForRoute(alias, type, getParams);
  }

  /**
   * Returns the current generated CSRF token
   *
   * @return the CSRF token
   */
  public String getServerCsrfToken()
  {
    HttpSession session = mServletRequest.getSession(false);
    if (session != null && session.getAttribute(SNAP_CSRF_TOKEN) != null)
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
    HttpSession session = mServletRequest.getSession(false);
    if (session != null && session.getAttribute(SNAP_CSRF_TOKEN) != null)
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

  private boolean mPersistLanguage = false;

  private HttpMethod mMethod;
  private RouteMatcher mRoute;
  private Long mAuthenticatedUser;
  private Router mRouter;

  private HttpSession mSession;

  private Locale mLocale = null;

  private String mLanguage = null;
}
