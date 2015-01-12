package snap.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.WebApplication;
import snap.http.HttpResponse;

public class ErrorView extends TemplateView
{
  final Logger log = LoggerFactory.getLogger(ErrorView.class);

  private static final String ERROR_PAGE_NAME = "snap-error.html";

  public ErrorView(String message)
  {
    super(ERROR_PAGE_NAME);
    addParameter("message", message);
  }

  public ErrorView(String message, Throwable t)
  {
    this(message);
    addParameter("exception", t);
  }

  public ErrorView(String message, int httpStatusCode)
  {
    this(message);
    mHttpStatusCode = httpStatusCode;
  }

  // TODO: add debug setting. If debug, do not show any internal code.
  @Override
  public void render(HttpResponse response) throws RenderException, IOException
  {
    InputStream in = getClass().getClassLoader().getResourceAsStream(
        mTemplateName);
    String template = StreamToString(in);

    addParameter("statuscode", Integer.valueOf(mHttpStatusCode));

    HttpServletResponse r = response.getResponse();

    r.setStatus(mHttpStatusCode);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");
    
    PrintWriter pw = response.getResponse().getWriter();
    pw.print(WebApplication.Instance().getRenderEngine()
        .render(template, mContext));

  }

  private int mHttpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

}
