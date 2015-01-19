package snap.http;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import snap.Route;
import snap.Router;

public class HttpRedirect implements RequestResult
{

  /**
   * Redirect the user to the url identified by alias
   * 
   * @param routeAlias
   * @param params
   */
  public HttpRedirect(String routeAlias, Object... params)
  {
    Route route = Router.instance().getRoute(routeAlias);
    mUrl = route.getLink(params);
  }

  /**
   * Redirect the user to the url identified by alias
   * 
   * @param routeAlias
   * @param params
   */
  public HttpRedirect(String routeAlias, Map<String, String> getParams,
      Object... params)
  {
    Route route = Router.instance().getRoute(routeAlias);
    mUrl = route.getLink(getParams, params);
  }

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
