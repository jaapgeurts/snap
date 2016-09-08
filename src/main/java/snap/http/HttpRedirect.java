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
    this(URI.create(url));
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
    this(URI.create(url), redirectType);
  }

  /**
   * Redirect the user to the url identified by URL
   *
   * @param url
   *          The url
   */
  public HttpRedirect(URL url)
  {
    this(URI.create(url.toExternalForm()));
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
    this(URI.create(url.toExternalForm()), redirectType);
  }

  /**
   * Redirect the user to the uri identified by URI
   *
   * @param uri
   *          The URI
   */
  public HttpRedirect(URI uri)
  {
    mUri = uri;
    mRedirectType = RedirectType.TEMPORARY_ALLOW_CHANGE;
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
    mUri = uri;
    mRedirectType = redirectType;
  }

  public URI getURI()
  {
    return mUri;
  }

  public RedirectType getRedirectType()
  {
    return mRedirectType;
  }

  @Override
  public String toString()
  {
    return "Status: " + getRedirectCode() + ", Location: " + mUri.toString();
  }

  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // mServletResponse.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    // mServletResponse.setHeader("Location", route.getLink(params));

    int code;

    HttpServletResponse response = context.getResponse();
    code = getRedirectCode();

    response.resetBuffer();
    response.setHeader("Location", mUri.normalize().toString());
    response.setStatus(code);
  }

  private int getRedirectCode()
  {
    int code;

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

    return code;
  }

  private URI mUri;
  private RedirectType mRedirectType;

}
