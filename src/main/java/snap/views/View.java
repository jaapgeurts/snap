package snap.views;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.RequestContext;
import snap.http.RequestResult;

public abstract class View implements RequestResult
{
  final Logger log = LoggerFactory.getLogger(View.class);

  public void handleResult(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();
    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    // TODO: should I try a catch here, and then change the response error code?
    try
    {
      render(context);
    }
    catch (Exception e)
    {
      log.warn("Rendering error: " + e.getMessage(), e);
      throw e;
    }
  }

  public abstract void render(RequestContext context) throws IOException;

}
