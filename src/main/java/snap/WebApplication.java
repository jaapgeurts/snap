package snap;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.rythmengine.RythmEngine;

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
