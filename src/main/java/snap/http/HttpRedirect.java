package snap.http;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

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
