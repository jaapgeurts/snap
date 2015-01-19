package snap;

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
  public static String siteRootUrl = "http://localhost";
  public static boolean debug = true;
  public static String emailTemplatePath;

  public static boolean threadSafeController = false;

  public static String rootPath;

  static
  {
    try
    {
      InputStream in = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("snap.properties");
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

        t = p.getProperty("snap.site.rooturl");
        if (t != null)
          siteRootUrl = new String(t);

        t = p.getProperty("snap.mail.templatepath");
        if (t != null)
          emailTemplatePath = new String(t);

        t = p.getProperty("snap.controller.threadsafe");
        if (t != null)
          threadSafeController = Boolean.valueOf(t);

        t = p.getProperty("snap.site.debug");
        if (t != null)
          debug = Boolean.parseBoolean(t);

        mProperties = p;
      }
      finally
      {
        if (in != null)
          in.close();
      }
    }
    catch (IOException e)
    {
      log.warn("Can't read settings.", e);
    }

  }

  public static Properties asProperties()
  {
    Properties p = new Properties();
    p.putAll(mProperties);
    return p;
  }

  private static Properties mProperties;

}
