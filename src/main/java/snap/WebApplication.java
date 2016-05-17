package snap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.rythmengine.RythmEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import snap.http.Authenticator;
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
    mAuthenticators = new ArrayList<>();
  }

  public void init(ServletConfig config)
  {

    mServletContext = config.getServletContext();

    String rootPath = mServletContext.getRealPath(".");
    Settings.rootPath = rootPath;

    // Setup the template engine
    // RythmEngine not thread safe?? see bug:
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
    mEngine.registerFastTag(new snap.rythm.FieldId());

    // todo: investigate adding custom resource loader to solve loading Router

  }

  /**
   * Default error handling
   * 
   * @param context
   *          The request context
   * @param errorCode
   *          The error code
   * @param exception
   *          the exception that was thrown
   * @throws IOException
   *           can throw an expection. ie. when the stream was closed
   *           prematurely
   * @return the view to display
   */
  public View handleError(RequestContext context, int errorCode, Throwable exception) throws IOException
  {
    HttpServletResponse response = context.getResponse();
    if (errorCode == HttpServletResponse.SC_UNAUTHORIZED)
    {
      for (Authenticator authenticator : getAuthenticators())
      {
        response.addHeader("WWW-Authenticate", authenticator.getWWWAuthenticateHeader());
      }
    }
    if (Settings.debug && exception != null)
    {
      response.sendError(errorCode, exception.getMessage());
    }
    else
    {
      response.sendError(errorCode);
    }

    return NullView.INSTANCE;

  }

  /**
   * Return the user object from your persistance store.
   * 
   * @param userid
   *          The ID of the user to fetch
   * @return the User or NULL
   */
  public User getUser(Long userid)
  {
    return null;
  }

  /**
   * Adds an authenticator to the web application. These are authenticators that
   * 
   * @param authenticator
   *          The authenticator to add
   */
  public void addAuthenticator(Authenticator authenticator)
  {
    mAuthenticators.add(authenticator);
  }

  /**
   * Returns the list of all authenticators known to the webapp
   * 
   * @return a list of all authenticators
   */
  public List<Authenticator> getAuthenticators()
  {
    return mAuthenticators;
  }

  public void destroy()
  {

  }

  /**
   * Return the Web app properties
   * 
   * @return The properties of the application
   */
  public static Properties getProperties()
  {
    Properties p = new Properties();
    p.putAll(mWebAppProperties);
    return p;
  }

  /**
   * Returns the installed Rythm engine
   * 
   * @return The current rendering engine
   */
  public RythmEngine getRenderEngine()
  {
    return mEngine;
  }

  /**
   * Gets the application servlet context
   * 
   * @return the servlet context
   */
  public ServletContext getContext()
  {
    return mServletContext;
  }

  /**
   * Returns the default Jackson JSON mapper. This is used by the Snap!'s
   * JsonView TODO: make this a factory method that can return any kind of json
   * mapper
   * 
   * @return the Jackson JSON mapper
   */
  public ObjectMapper getJsonMapper()
  {
    ObjectMapper mapper = new ObjectMapper();

    return mapper;
  }

  /**
   * Setup a listener for request pre post processing hooks. You must set this
   * listiner in the Init() call. Setting it after Init() has no effect.
   * 
   * @param listener
   *          the listener to add
   */
  public void setRequestListener(RequestListener listener)
  {
    mRequestListener = listener;
  }

  /**
   * Returns the install request listener
   * 
   * @return the installed listener
   */
  public RequestListener getRequestListener()
  {
    return mRequestListener;
  }

  /**
   * Reads the properties of the web app. By default names "webapp.properties"
   * 
   * @return the properies
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
   *          The path to read the properties from
   * @return The properties
   */
  // FIXME: this should not be named so and read properties for the snap user
  // should not be stored in this class but in the users own derived webbapp
  // class
  protected Properties readProperties(String filePath)
  {
    mWebAppProperties = null;
    try
    {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
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

  private List<Authenticator> mAuthenticators;
  private static Properties mWebAppProperties = null;

}
