package com.proficiosoftware.snap.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proficiosoftware.snap.WebApplication;
import com.proficiosoftware.snap.http.HttpResponse;

public class ErrorView extends TemplateView
{
  final Logger log = LoggerFactory.getLogger(ErrorView.class);

  private static final String ERROR_PAGE_NAME = "snap-error.html";

  public ErrorView(String message, Throwable t)
  {
    super(ERROR_PAGE_NAME);
    addParameter("exception", t);
    addParameter("message", message);
  }

  public ErrorView(String message)
  {
    super(ERROR_PAGE_NAME);
    addParameter("message", message);
  }

  @Override
  public void render(HttpResponse response) throws RenderException, IOException
  {
    InputStream in = getClass().getClassLoader().getResourceAsStream(
        mTemplateName);

    String template = StreamToString(in);
    
    PrintWriter pw = response.getResponse().getWriter();
    
    pw.print(WebApplication.Instance().getRenderEngine()
        .render(template, mContext));

    HttpServletResponse r = response.getResponse();
    
    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("text/html");

  }

}
