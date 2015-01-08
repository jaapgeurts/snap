package com.proficiosoftware.snap;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.rythmengine.RythmEngine;

public class WebApplication
{

  public static WebApplication Instance()
  {
    return mWebApplication;
  }

  public void init(ServletConfig config)
  {
    mWebApplication = this;

    mServletContext = config.getServletContext();
    
    // Setup the template engine
    Map<String, Object> conf = new HashMap<String, Object>();
    conf.put("engine.mode", "dev");
    conf.put("home.template",mServletContext.getRealPath("."));
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
