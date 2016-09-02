package snap;

import java.util.Map;

import snap.http.HttpMethod;
import snap.http.RequestContext;
import snap.http.RequestResult;

public interface Route
{
  /**
   * You must implement this method and do any initialization as if this was the
   * constructor.
   *
   * @param contextPath
   *          The path under which this app is running on the servlet
   * @param alias
   *          The alias by which this route is known
   * @param url
   *          The url as declared in the routes.conf
   * @param field4
   *          The last column in the routes.conf file.
   */
  void init(String contextPath, String alias, String url, String field4);

  /**
   * Indicated whether your route should redirect to the snap.login.redirect.url
   * when the user is not authenticated but required
   *
   * @return true or false
   */
  boolean isRedirectEnabled();

  /**
   * Called when your route is executed.
   *
   * @param context
   *          The request context
   * @return The result your wish to return
   * @throws Throwable
   */
  RequestResult handleRoute(RequestContext context) throws Throwable;

  /**
   * Get the HTTP methods supported by this route.
   *
   * @return the list of supported HttpMethods
   */
  HttpMethod[] getHttpMethods();

  /**
   * Called when snap needs a URL for this route (often called when @link() is
   * called in a template). Snap will precompute the url and you can modify it
   * if you wish.
   *
   * @param original
   *          The url as computed by snap ( the regex groups have been replaced
   *          by the params)
   * @param getParams
   *          The get params to be appended to this url
   * @param params
   *          the params to be replaced in the path section of the URL
   * @return
   */
  String getLink(String original, Map<String, Object> getParams, Object[] params);
}
