package snap.http;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

/**
 * Because this class implements RequestResult you can return this class from a
 * router method. There are three ways to obtain this class.
 * <ol>
 * <li>Create it directly. - This is useful if you wish to redirect to an user
 * defined URL.</li>
 * <li>Call RequestContext.getRoute().getRedirect() and you will get a redirect
 * to the same URL that invoked the current controller method</li>
 * <li>Call RequestContext.getRouter().getRedirect() and you can get a redirect
 * for any route specified in your routes.conf file.</li>
 * </ol>
 *
 * @author Jaap Geurts
 *
 */
public class HttpRedirect implements RequestResult
{

  /**
   * Redirect the user to the url identified by string URL
   *
   * @param url
   *          The url to redirect to
   *
   */
  public HttpRedirect(String url)
  {
    this(url, RedirectType.TEMPORARY_ALLOW_CHANGE);
  }

  /**
   * Redirect the user to the url identified by string URL
   *
   * @param url
   *          The url to redirect to
   * @param redirectType
   *          The type of redirect to use
   */
  public HttpRedirect(String url, RedirectType redirectType)
  {
    if (url == null)
      throw new IllegalArgumentException();
    mUrl = url;
    mRedirectType = redirectType;
  }

  /**
   * Redirect the user to the url identified by URL
   *
   * @param url
   *          The url
   */
  public HttpRedirect(URL url)
  {
    this(url.toString());
  }

  /**
   * Redirect the user to the url identified by URL
   *
   * @param url
   *          The url to redirect to
   * @param redirectType
   *          The type of redirect to use
   */
  public HttpRedirect(URL url, RedirectType redirectType)
  {
    this(url.toString(), redirectType);
  }

  /**
   * Redirect the user to the uri identified by URI
   *
   * @param uri
   *          The URI
   */
  public HttpRedirect(URI uri)
  {
    this(uri.toString());
  }

  /**
   * Redirect the user to the uri identified by URI
   *
   * @param uri
   *          The uri to redirect to
   * @param redirectType
   *          The type of redirect to use
   *
   */
  public HttpRedirect(URI uri, RedirectType redirectType)
  {
    this(uri.toString(), redirectType);
  }

  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // mServletResponse.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    // mServletResponse.setHeader("Location", route.getLink(params));

    int code;

    HttpServletResponse response = context.getResponse();
    switch(mRedirectType)
    {
      case PERMANENT_ALLOW_CHANGE:
        code = HttpServletResponse.SC_MOVED_PERMANENTLY; // 301
        break;
      case PERMANENT_FORCE_GET:
        code = HttpServletResponse.SC_SEE_OTHER;// 303
        break;
      case TEMPORARY_FORCE_SAME:
        code = HttpServletResponse.SC_TEMPORARY_REDIRECT; // 307
        break;
      case PERMANENT_FORCE_SAME:
        code = 308;
        break;
      case TEMPORARY_ALLOW_CHANGE:
      default:
        code = HttpServletResponse.SC_FOUND; // 302
        break;
    }

    response.resetBuffer();
    response.setHeader("Location", mUrl);
    response.setStatus(code);
  }

  private String mUrl;
  private RedirectType mRedirectType;

}
