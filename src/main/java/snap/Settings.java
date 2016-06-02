package snap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings
{
  final static Logger log = LoggerFactory.getLogger(Settings.class);

  public enum LocaleStorageMode {
    COOKIE, SESSION, DATABASE
  };

  public static String routesFile = "routes.conf";
  public static String packagePrefix;
  public static String webAppClass = null;
  public static String redirectUrl = "/";
  public static String siteRootUrl = "http://localhost";
  public static boolean debug = true;
  public static String emailTemplatePath;
  public static String rythmEngineMode = "dev"; // defaults to dev mode
  public static LocaleStorageMode localeMode = LocaleStorageMode.COOKIE;

  public static boolean threadSafeController = false;

  public static String rootPath;

  public static boolean redirectEnabled;

  static
  {
    try
    {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("snap.properties");
      Properties p = new Properties();
      try
      {
        p.load(in);
        String t = p.getProperty("snap.router.routes");
        if (t != null)
          routesFile = new String(t);

        t = p.getProperty("snap.router.packageprefix");
        if (t != null)
          packagePrefix = new String(t);

        webAppClass = p.getProperty("snap.applicationclass");

        t = p.getProperty("snap.login.redirect.url");
        if (t != null)
          redirectUrl = new String(t);

        t = p.getProperty("snap.login.redirect");
        if (t != null)
          redirectEnabled = Boolean.parseBoolean(t);

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

        t = p.getProperty("rythm.engine.mode");
        if (t != null)
        {
          if (!"dev".equals(t.toLowerCase()) && !"prod".equals(t.toLowerCase()))
            log.warn("Invalid value for 'rythm.engine.mode'. Legal values are 'dev' or 'prod'. Defaulting to 'dev'");
          else
            rythmEngineMode = new String(t);
        }

        t = p.getProperty("snap.site.localemode");
        try
        {
          LocaleStorageMode.valueOf(t.toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e)
        {
          log.warn("Missing or invalid value for 'snap.site.localemode'. legal values are 'cookie', 'session' or 'database'. Defaulting to 'cookie'");
        }

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

  public static int getInt(String key)
  {
    return Integer.valueOf(mProperties.getProperty(key));
  }

  public static String get(String key)
  {
    return mProperties.getProperty(key);
  }

  public static Properties asProperties()
  {
    Properties p = new Properties();
    p.putAll(mProperties);
    return p;
  }

  private static Properties mProperties;

}
