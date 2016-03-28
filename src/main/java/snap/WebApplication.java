package snap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.rythmengine.RythmEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.RequestContext;
import snap.views.NullView;
import snap.views.View;

public abstract class WebApplication
{

  final Logger log = LoggerFactory.getLogger(WebApplication.class);

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
    // TODO: RythmEngine not thread safe?? see bug:
    // https://github.com/greenlaw110/Rythm/issues/20
    Properties conf = new Properties();
    conf.put("engine.mode", "dev");
    conf.put("home.template", rootPath);
    mEngine = new RythmEngine(conf);

    // register snap custom transformers

    // register snap custom tags
    mEngine.registerFastTag(new snap.rythm.Form());
    mEngine.registerFastTag(new snap.rythm.Field());
    mEngine.registerFastTag(new snap.rythm.FieldError());
    mEngine.registerFastTag(new snap.rythm.Label());
    mEngine.registerFastTag(new snap.rythm.DynamicRelativeLink());
    mEngine.registerFastTag(new snap.rythm.DynamicRootLink());
    mEngine.registerFastTag(new snap.rythm.Csrf_Token());
    mEngine.registerFastTag(new snap.rythm.LinebreaksToParagraph());
    mEngine.registerFastTag(new snap.rythm.FormErrors());

    // todo: investigate adding custom resource loader to solve loading Router

  }

  /**
   * Default error handling
   * 
   * @param context
   * @param errorCode
   * @param exception
   * @throws IOException
   */
  public View handleError(RequestContext context, int errorCode,
      Throwable exception) throws IOException
  {
    HttpServletResponse response = context.getResponse();
    if (errorCode == HttpServletResponse.SC_UNAUTHORIZED)
      response.setHeader("WWW-Authenticate", "Basic realm=\"snap\"");
    if (exception == null)
    {
      response.sendError(errorCode);
    }
    else
    {
      if (Settings.debug)
        response.sendError(errorCode, exception.getMessage());
      else
        response.sendError(errorCode);
    }

    return NullView.INSTANCE;
  }

  /**
   * Return the user object from your persistance store.
   * 
   * @param userid
   * @return
   */
  public User getUser(Long userid)
  {
    return null;
  }

  /**
   * Authenticate the user with the credentials. Call setAuthenticatedUser() on
   * context if authentication was successful and return true. False if
   * authentication failed
   * 
   * @param context
   * @param username
   * @param password
   * @return
   */
  public boolean authenticateUser(RequestContext context, String username,
      String password)
  {
    return false;
  }

  public void destroy()
  {

  }

  /**
   * Return the Web app properties
   * 
   * @return
   */
  public Properties getProperties()
  {
    Properties p = new Properties();
    p.putAll(mWebAppProperties);
    return p;
  }

  /**
   * Returns the installed Rythm engine
   * 
   * @return
   */
  public RythmEngine getRenderEngine()
  {
    return mEngine;
  }

  /**
   * Gets the application servlet context
   * 
   * @return
   */
  public ServletContext getContext()
  {
    return mServletContext;
  }

  /**
   * Setup a listener for request pre post processing hooks. You must set this
   * listiner in the Init() call. Setting it after Init() has no effect.
   * 
   * @param listener
   */
  public void setRequestListener(RequestListener listener)
  {
    mRequestListener = listener;
  }

  /**
   * Returns the install request listener
   * 
   * @return
   */
  public RequestListener getRequestListener()
  {
    return mRequestListener;
  }

  /**
   * Reads the properties of the web app. By default names "webapp.properties"
   * 
   * @return
   */
  protected Properties readProperties()
  {
    return readProperties("webapp.properties");
  }

  /**
   * Reads properties from the system path (ie. your resources in WEB-INF
   * classes
   * 
   * @param filePath
   * @return The properties
   */
  protected Properties readProperties(String filePath)
  {
    mWebAppProperties = null;
    try
    {
      InputStream in = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(filePath);
      mWebAppProperties = new Properties();
      try
      {
        mWebAppProperties.load(in);
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
    return getProperties();
  }

  private RythmEngine mEngine;
  private ServletContext mServletContext;

  private static WebApplication mWebApplication = null;
  private RequestListener mRequestListener;

  private Properties mWebAppProperties = null;

}
