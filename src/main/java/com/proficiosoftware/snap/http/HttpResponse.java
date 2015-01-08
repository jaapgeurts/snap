package com.proficiosoftware.snap.http;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proficiosoftware.snap.Route;
import com.proficiosoftware.snap.Router;

public class HttpResponse
{
  final static Logger log = LoggerFactory.getLogger(HttpResponse.class);

  public HttpResponse(HttpServletResponse response)
  {
    mServletResponse = response;
  }

  /**
   * Redirect the user to the url identified by alias
   * 
   * @param routeAlias
   * @param params
   */
  public void redirect(String routeAlias, Object... params)
  {
    Route route = Router.instance().getRoute(routeAlias);
    // mServletResponse.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    // mServletResponse.setHeader("Location", route.getLink(params));
    if (route == null)
    {
      log.debug("Invalid route: %s\n", routeAlias);
      throw new RuntimeException("Can't redirect: Unknown route: " + routeAlias);
    }

    try
    {
      mServletResponse.sendRedirect(route.getLink(params));
    }
    catch (IOException e)
    {
      log.debug("Can't redirect.", e);
    }
  }

  /**
   * Redirect the user to the url identified by alias
   * 
   * @param routeAlias
   * @param params
   */
  public void redirect(String routeAlias, Map<String, String> getParams,
      Object... params)
  {
    Route route = Router.instance().getRoute(routeAlias);
    // mServletResponse.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    // mServletResponse.setHeader("Location", route.getLink(params));
    if (route == null)
    {
      log.debug("Invalid route: %s\n", routeAlias);
      throw new RuntimeException("Can't redirect: Unknown route: " + routeAlias);
    }

    try
    {
      String link = route.getLink(getParams, params);
      mServletResponse.sendRedirect(link);
    }
    catch (IOException e)
    {
      log.debug("Can't redirect.", e);
    }
  }

  /**
   * Redirect the user to the url identified by string URL
   * 
   * @param routeAlias
   * @param params
   */
  public void redirectUrl(String url)
  {
    try
    {
      mServletResponse.sendRedirect(url);
    }
    catch (IOException e)
    {
      log.debug("Can't redirect.", e);
    }
  }


  public HttpServletResponse getResponse()
  {
    return mServletResponse;
  }

  private HttpServletResponse mServletResponse;
}
