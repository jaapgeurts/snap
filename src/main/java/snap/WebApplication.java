package snap;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.rythmengine.RythmEngine;

import snap.http.RequestContext;
import snap.views.NullView;
import snap.views.View;

public abstract class WebApplication
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
    mEngine.registerFastTag(new snap.rythm.DynamicRelativeLink());
    mEngine.registerFastTag(new snap.rythm.DynamicRootLink());
    mEngine.registerFastTag(new snap.rythm.Csrf_Token());
    mEngine.registerFastTag(new snap.rythm.LinebreaksToParagraph());

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
    if (exception == null)
      response.sendError(errorCode);
    else
      response.sendError(errorCode, exception.getMessage());

    return NullView.INSTANCE;
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
