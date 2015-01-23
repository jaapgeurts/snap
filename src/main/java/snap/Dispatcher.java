package snap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.forms.InvalidCsrfToken;
import snap.forms.MissingCsrfToken;
import snap.http.HttpError;
import snap.http.HttpMethod;
import snap.http.RequestContext;
import snap.http.RequestResult;

public class Dispatcher extends HttpServlet
{

  final Logger log = LoggerFactory.getLogger(Dispatcher.class);

  public Dispatcher()
  {
  }

  // public Dispatcher(String webappClass) throws InstantiationException,
  // IllegalAccessException, ClassNotFoundException
  // {
  // mWebAppClass = webappClass;
  // mWebApplication =
  // (WebApplication)Class.forName(mWebAppClass).newInstance();
  // }

  @Override
  public void init(ServletConfig config) throws ServletException
  {
    // Setup the router
    mRouter = Router.getInstance();

    try
    {
      mRouter.init(config.getServletContext().getContextPath());
    }
    catch (FileNotFoundException e)
    {
      throw new ServletException("Cannot open route file", e);
    }

    // force loading of settings so that the static initializer is called
    try
    {
      Class.forName("snap.Settings");
      mWebApplication = (WebApplication)Class.forName(Settings.webAppClass)
          .newInstance();
    }
    catch (ClassNotFoundException | InstantiationException
        | IllegalAccessException e)
    {
      throw new ServletException(e);
    }

    mWebApplication.init(config);
  }

  /* forward all requests to a single point of entry */
  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.HEAD, request, response));
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.GET, request, response));
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.POST, request, response));
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.PUT, request, response));
  }

  @Override
  protected void doDelete(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.DELETE, request, response));
  }

  @Override
  protected void doOptions(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.OPTIONS, request, response));
  }

  @Override
  protected void doTrace(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.TRACE, request, response));
  }

  private void handleRequest(RequestContext context) throws ServletException,
      IOException
  {

    context.setRouter(mRouter);
    HttpServletRequest request = context.getRequest();
    HttpServletResponse response = context.getResponse();

    if (request.getCharacterEncoding() == null)
      request.setCharacterEncoding("UTF-8");
    // match the path here and find a route
    String path = request.getPathInfo();
    if (path == null || "".equals(path))
    {
      String message = "the url-pattern section for this servlet in web.xml should be '/*'";
      log.error(message);
      throw new ServletException(message);
    }

    HttpMethod method = context.getMethod();

    RequestResult requestResult = null;
    RequestResult errorResult = null;

    try
    {

      Route route = mRouter.findRouteForPath(method, path);
      context.setRoute(route);

      log.debug(String.format("%s - %s", method, path));

      context.addParameters(route.getParameters(path));
      // Ask the controller to process the request
      requestResult = route.handleRoute(context);
      // Process the returned result of the controller.
      requestResult.handleResult(context);

    }
    catch (MissingCsrfToken mct)
    {
      errorResult = new HttpError(HttpServletResponse.SC_BAD_REQUEST,
          "CsrfToken missing", mct);
    }
    catch (InvalidCsrfToken ict)
    {
      errorResult = new HttpError(HttpServletResponse.SC_BAD_REQUEST,
          "CsrfToken invalid", ict);
    }
    catch (HttpMethodException hme)
    {
      errorResult = new HttpError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
          "Incorrect Http Method", hme);
    }
    catch (RouteNotFoundException rnfe)
    {
      errorResult = new HttpError(HttpServletResponse.SC_NOT_FOUND,
          "Route not found", rnfe);
    }
    catch (ResourceNotFoundException rnfe)
    {
      errorResult = new HttpError(HttpServletResponse.SC_NOT_FOUND,
          "Resource not found", rnfe);
    }
    catch (AuthorizationException ae)
    {
      log.debug("User not authorized access", ae.getMessage());
      errorResult = new HttpError(HttpServletResponse.SC_FORBIDDEN,
          "User not authorized", ae);
    }
    catch (AuthenticationException uae)
    {
      // redirect to redirect URL
      log.debug("User not logged in, redirecting: {}", uae.getMessage());
      String url = Settings.redirectUrl;
      String query = request.getQueryString();
      String next;
      if (query != null)
        next = path + "?" + query;
      else
        next = path;
      // encode the path
      response.sendRedirect(url + "?next=" + URLEncoder.encode(next, "UTF-8"));
    }
    catch (SnapException se)
    {
      errorResult = new HttpError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          se.getMessage(), se);
    }
    catch (Throwable t)
    {
      // Catch everything and report it in the browser.
      // If we really can't handle it then bail
      // TODO: Load error view
      errorResult = new HttpError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Server Error", t);

      log.error(t.getMessage(), t);
    }

    // If requestresult != null then an error occurred;
    if (errorResult != null)
    {
      try
      {
        errorResult.handleResult(context);
      }
      catch (IOException ioe)
      {
        log.error("Rendering of error also failed", ioe);
      }
    }

  }

  @Override
  public void destroy()
  {
    super.destroy();
    mWebApplication.destroy();
  }

  public WebApplication getWebApplication()
  {
    return mWebApplication;
  }

  public void setWebApplication(WebApplication mWebApplication)
  {
    this.mWebApplication = mWebApplication;
  }

  private Router mRouter;
  private WebApplication mWebApplication;

}
