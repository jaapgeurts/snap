package snap.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.Router;
import snap.Settings;
import snap.WebApplication;
import snap.views.TemplateView;
import snap.views.View;

public class HttpError implements RequestResult
{
  final static Logger log = LoggerFactory.getLogger(Router.class);

  private static final String ERROR_PAGE_NAME = "snap-error.html";

  public HttpError(int errorCode, String message)
  {
    mErrorCode = errorCode;
    mMessage = message;
  }

  public HttpError(int errorCode, String message, Throwable t)
  {
    this(errorCode, message);
    mException = t;
  }

  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // Check properties.

    // If debug = true, render the result here.
    if (Settings.debug)
    {
      InputStream in = getClass().getClassLoader().getResourceAsStream(
          ERROR_PAGE_NAME);
      String template = StreamToString(in);

      Map<String, Object> map = new HashMap<>();
      map.put("exception", mException);
      map.put("statuscode", mErrorCode);
      map.put("message", mMessage);

      HttpServletResponse r = context.getResponse();
      r.setStatus(mErrorCode);
      r.setContentType("text/html; charset=UTF-8");
      r.setCharacterEncoding("UTF-8");

      String s = WebApplication.getInstance().getRenderEngine()
          .render(template, map);
      PrintWriter writer = r.getWriter();
      writer.print(s);

    }
    else
    {
      // Else call the User's Web application display custom error pages
      // The default implementation just returns the error to the Servlet
      // Container
      WebApplication webApp = WebApplication.getInstance();
      webApp.handleError(context, mErrorCode, mException);
    }
  }

  protected String StreamToString(InputStream in) throws IOException
  {
    if (in == null)
    {
      log.warn("Inputstream argument can't be null");
      throw new IllegalArgumentException("Can't read template: "
          + ERROR_PAGE_NAME);
    }
    BufferedReader br;
    StringBuilder builder = new StringBuilder();
    try
    {
      br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

      char[] buffer = new char[2048];
      int len;
      len = br.read(buffer);
      while (len != -1)
      {
        builder.append(buffer, 0, len);
        len = br.read(buffer);
      }
      return builder.toString();
    }
    catch (UnsupportedEncodingException e)
    {
      log.error("JVM doesn't support UTF-8 encoding: template: "
          + ERROR_PAGE_NAME, e);
      throw e;
    }
    catch (IOException e)
    {
      log.error("IO Exception reading template: " + ERROR_PAGE_NAME, e);
      throw e;
    }
  }

  private int mErrorCode;
  private String mMessage;
  private Throwable mException;

}
