package com.proficiosoftware.snap.views;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proficiosoftware.snap.WebApplication;

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
  public CharSequence render() throws RenderException
  {
    InputStream in = getClass().getClassLoader().getResourceAsStream(
        mTemplateName);

    String template = StreamToString(in);
    return WebApplication.Instance().getRenderEngine()
        .render(template, mContext);
  }

}
