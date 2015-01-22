package snap.http;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class HttpRedirect implements RequestResult
{

  /**
   * Redirect the user to the url identified by string URL
   * 
   * @param routeAlias
   * @param params
   */
  public HttpRedirect(String url)
  {
    mUrl = url;
  }
  
  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // mServletResponse.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    // mServletResponse.setHeader("Location", route.getLink(params));

    HttpServletResponse response = context.getResponse();
    response.sendRedirect(mUrl);
  }

  private String mUrl;

}
