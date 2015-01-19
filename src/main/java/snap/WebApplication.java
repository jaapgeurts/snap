package snap;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.rythmengine.RythmEngine;

import snap.http.RequestContext;

public class WebApplication
{

  public static WebApplication getInstance()
  {
    return mWebApplication;
  }

  public WebApplication()
  {
    mWebApplication = this;
  }

  public void init(ServletConfig config)
  {

    mServletContext = config.getServletContext();

    String rootPath = mServletContext.getRealPath(".");
    Settings.rootPath = rootPath;

    // Setup the template engine
    Properties conf = new Properties();
    conf.put("engine.mode", "dev");
    conf.put("home.template", rootPath);
    mEngine = new RythmEngine(conf);

  }

  /**
   * Default error handling
   * 
   * @param context
   * @param errorCode
   * @param exception
   * @throws IOException
   */
  public void handleError(RequestContext context, int errorCode,
      Throwable exception) throws IOException
  {
    HttpServletResponse response = context.getResponse();
    if (exception == null)
      response.sendError(errorCode);
    else
      response.sendError(errorCode, exception.getMessage());

  }

  public void destroy()
  {

  }

  public RythmEngine getRenderEngine()
  {
    return mEngine;
  }

  public ServletContext getContext()
  {
    return mServletContext;
  }

  private RythmEngine mEngine;
  private ServletContext mServletContext;

  private static WebApplication mWebApplication = null;

}
