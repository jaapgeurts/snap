package com.proficiosoftware.snap;

import java.util.HashMap;
import java.util.Map;

import org.rythmengine.Rythm;

public class WebApplication 
{

  public void init()
  {
    // Setup the template engine
    Map<String, Object> conf = new HashMap<String, Object>();
    conf.put("engine.mode", "dev");
    Rythm.init(conf);
  }

  public void destroy()
  {

  }

}
