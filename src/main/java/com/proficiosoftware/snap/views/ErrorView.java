package com.proficiosoftware.snap.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.rythmengine.Rythm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  public CharSequence render()
  {
    InputStream in = getClass().getClassLoader().getResourceAsStream(
        ERROR_PAGE_NAME);
    if (in == null)
    {
      log.warn("Error reading snap error template");
      throw new RuntimeException("Can't read template: " + ERROR_PAGE_NAME);
    }
    InputStreamReader isr;
    StringWriter sw = new StringWriter();
    try
    {
      isr = new InputStreamReader(in, "UTF-8");

      char[] buffer = new char[2048];
      int len;
      len = isr.read(buffer);
      while (len != -1)
      {
        sw.write(buffer);
        len = isr.read(buffer);
      }
    }
    catch (UnsupportedEncodingException e)
    {
      log.warn("JVM doesn't support UTF-8 encoding", e);
      throw new RuntimeException("Can't read template: " + ERROR_PAGE_NAME, e);
    }
    catch (IOException e)
    {
      log.warn("Error reading snap error template", e);
      throw new RuntimeException("Can't read template: " + ERROR_PAGE_NAME, e);
    }
    return Rythm.render(sw.toString(), mContext);
  }

}
