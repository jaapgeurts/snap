package snap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings
{
  final static Logger log = LoggerFactory.getLogger(Settings.class);

  public enum LocaleMode {
    COOKIE, SESSION, SUBDOMAIN, CUSTOM
  };

  static String routesFile = "routes.conf";
  static String packagePrefix;
  static String webAppClass = null;
  static String redirectUrl = "/";
  static boolean threadSafeController = false;
  static boolean redirectEnabled = false;
  static String defaultLanguage = "en-US";
  static URI siteRootUri;

  // These should not be public (they are public for the parent package
  public static boolean debug = true;
  public static String emailTemplatePath;
  public static String rythmEngineMode = "dev"; // defaults to dev mode
  public static LocaleMode localeMode = LocaleMode.COOKIE;

  public static String rootPath;

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
        {
          try
          {
            siteRootUri = new URI(t);
          }
          catch (URISyntaxException use)
          {
            log.warn("Invalid url for 'snap.site.rooturl': " + siteRootUri);
            throw new IllegalArgumentException(use);
          }
        }

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
            log.warn(
                "Invalid value for 'rythm.engine.mode'. Legal values are 'dev' or 'prod'. Defaulting to 'dev'");
          else
            rythmEngineMode = new String(t);
        }

        t = p.getProperty("snap.site.localemode");
        try
        {
          localeMode = LocaleMode.valueOf(t.toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e)
        {
          log.warn(
              "Missing or invalid value for 'snap.site.localemode'. legal values are 'cookie', 'session', 'subdomain' or 'custom'. Defaulting to 'cookie'");
        }

        t = p.getProperty("snap.site.locale.default");
        if (t != null)
          defaultLanguage = new String(t);

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

  public static String get(String key, String dflt)
  {
    String val = get(key);
    return val == null ? dflt : val;
  }

  public static int getInt(String key, int dflt)
  {
    String val = get(key);
    return val == null ? dflt : Integer.valueOf(val);
  }

}
