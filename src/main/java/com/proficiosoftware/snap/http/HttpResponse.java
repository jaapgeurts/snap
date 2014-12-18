package com.proficiosoftware.snap.http;

import java.io.IOException;
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

  public void redirect(String routeAlias, Object... params)
  {
    Route route = Router.instance().getRoute(routeAlias);
    // mServletResponse.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    // mServletResponse.setHeader("Location", route.getLink(params));
    try
    {
      mServletResponse.sendRedirect(route.getLink(params));
    }
    catch (IOException e)
    {
      log.debug("Can't redirect.", e);
    }
  }

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
