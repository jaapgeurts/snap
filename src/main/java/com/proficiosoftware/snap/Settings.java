package com.proficiosoftware.snap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings
{
  final static Logger log = LoggerFactory.getLogger(Settings.class);

  public static String routesFile = "routes.conf";
  public static String webAppClass = null;
  public static String redirectUrl = "/";

  static
  {
    InputStream in = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("settings.properties");
    Properties p = new Properties();
    try
    {
      p.load(in);
      String t = p.getProperty("snap.routes");
      if (t != null)
        routesFile = new String(t);

      webAppClass = p.getProperty("snap.applicationclass");

      t = p.getProperty("snap.login.redirecturl");
      if (t != null)
        redirectUrl = new String(t);

      in.close();
    }
    catch (IOException e)
    {
      log.warn("Can't read settings.", e);
    }
  }

}
