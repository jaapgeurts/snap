package snap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.forms.MissingCsrfTokenException;
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
      mWebApplication = (WebApplication)Class.forName(Settings.webAppClass).newInstance();
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
    {
      throw new ServletException(e);
    }

    mWebApplication.init(config);

    // cache the field here so we don't have to do a lookup each time
    mRequestListener = mWebApplication.getRequestListener();
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
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.DELETE, request, response));
  }

  @Override
  protected void doOptions(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.OPTIONS, request, response));
  }

  @Override
  protected void doTrace(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(new RequestContext(HttpMethod.TRACE, request, response));
  }

  private void handleRequest(RequestContext context) throws ServletException, IOException
  {

    context.setRouter(mRouter);
    HttpServletRequest request = context.getRequest();
    HttpServletResponse response = context.getResponse();

    if (request.getCharacterEncoding() == null)
      request.setCharacterEncoding("UTF-8");
    // match the path here and find a route
    String path = context.getRequestURI();
    if (path == null || "".equals(path))
    {
      String message = "The url-pattern section for this servlet in web.xml should be '/*'";
      log.error(message);
      throw new ServletException(message);
    }

    HttpMethod method = context.getMethod();

    RequestResult requestResult = null;
    RequestResult errorResult = null;

    try
    {

      RouteMatcher routeMatcher = mRouter.findRouteMatcherForPath(method, path);

      context.setRouteMatcher(routeMatcher);

      if (mRequestListener != null)
        mRequestListener.onBeforeRequest(context);

      // Ask the controller to process the request
      requestResult = routeMatcher.handleRoute(context);
      // Process the returned result of the controller.
      requestResult.handleResult(context);

      if (mRequestListener != null)
        mRequestListener.onAfterRequest(context);

    }
    catch (MissingCsrfTokenException mct)
    {
      errorResult = new HttpError(HttpServletResponse.SC_BAD_REQUEST, "CsrfToken missing", mct);
    }
    catch (InvalidCsrfTokenException ict)
    {
      errorResult = new HttpError(HttpServletResponse.SC_BAD_REQUEST, "CsrfToken invalid", ict);
    }
    catch (HttpMethodException hme)
    {
      errorResult = new HttpError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid http method", hme);
    }
    catch (RouteNotFoundException rnfe)
    {
      errorResult = new HttpError(HttpServletResponse.SC_NOT_FOUND, "Route not found", rnfe);
    }
    catch (ResourceNotFoundException rnfe)
    {
      errorResult = new HttpError(HttpServletResponse.SC_NOT_FOUND, "Resource not found", rnfe);
    }
    catch (AuthorizationException ae)
    {
      errorResult = new HttpError(HttpServletResponse.SC_FORBIDDEN, "User not authorized", ae);
    }
    catch (AuthenticationException uae)
    {
      // check if user wants redirects
      if (context.getRoute().isRedirectEnabled())
      {
        log.debug("User not logged in, redirecting: {}", uae.getMessage());
        // redirect to redirect URL
        Map<String, String> redirParams = new HashMap<>();
        String[] pathQuery = Settings.redirectUrl.split("\\?");
        if (pathQuery.length == 0)
        {
          log.warn("Redirect requested but 'snap.login.redirect.url' not set");
          throw uae;
        }
        String newPath = pathQuery[0];

        // Decode the query string parts from the redirectUrl
        if (pathQuery.length > 1)
        {
          redirParams.putAll(Arrays.stream(pathQuery[1].split("&")).map(Helpers::splitQueryParam)
              .collect(Collectors.toMap(SimpleImmutableEntry::getKey, e -> Helpers.encodeURL(e.getValue()))));
        }

        // decode the query string parts from the request string
        String query = request.getQueryString();

        String next = query != null ? path + "?" + query : path;
        // no need to encode as getQueryString() returns encoded values
        response.sendRedirect(newPath + "?next=" + URLEncoder.encode(next, "UTF-8") + "&" + redirParams
            .entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&")));
      }
      else
      {
        errorResult = new HttpError(HttpServletResponse.SC_UNAUTHORIZED, "You are not authenticated");
      }
    }
    catch (SnapException se)
    {
      log.error("Snap Framework error", se);
      errorResult = new HttpError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, se.getMessage(), se);
    }
    catch (Throwable t)
    {
      log.error("Uncaught exception", t);
      // Catch everything and report it in the browser.
      // If we really can't handle it then bail
      // Load error view
      errorResult = new HttpError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", t);
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
  private RequestListener mRequestListener;

}
