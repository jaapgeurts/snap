package com.proficiosoftware.snap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proficiosoftware.snap.http.HttpRequest;
import com.proficiosoftware.snap.http.HttpResponse;
import com.proficiosoftware.snap.views.ErrorView;
import com.proficiosoftware.snap.views.View;

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
    mRouter = Router.instance();

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
      Class.forName("com.proficiosoftware.snap.Settings");
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

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(HttpRequest.HTTP_POST, request, response);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    handleRequest(HttpRequest.HTTP_GET, request, response);
  }

  private void handleRequest(String method, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException
  {

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
    Route route = mRouter.findRouteForPath(method, path);

    log.debug(String.format("%s - %s", method, path));

    HttpRequest httpRequest = new HttpRequest(route, request, method);
    HttpResponse httpResponse = new HttpResponse(response);

    try
    {
      View view = null;
      if (route == null)
      {
        String message = "No routes matched path: " + path;
        view = new ErrorView(message, HttpServletResponse.SC_NOT_FOUND);
        log.warn(message);
      }
      else
      {
        httpRequest.addParameters(route.getParameters(path));
        view = route.handleRoute(httpRequest, httpResponse);
      }
      if (view != null)
      {
        try
        {
          view.render(httpResponse);
        }
        catch (Exception e)
        {
          String message = "Error during rendering";
          view = new ErrorView(message, e);
          view.render(httpResponse);
          log.warn(message);
        }
      }
    }
    catch (AuthorizationException ae)
    {
      log.debug("User not authorized access", ae.getMessage());
      // todo: consider how to return this to the framework so the user can show
      // their own error view
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
    catch (Throwable t)
    {
      // Catch everything and report it in the browser.
      // If we really can't handle it then bail
      // TODO: Load error view

      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setContentType("text/html; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");

      PrintWriter pw = response.getWriter();
      pw.print("<html><body><p><pre>");
      pw.print(t.getMessage());
      pw.println("</pre><br/><pre>");
      t.printStackTrace(pw);
      pw.print("</pre></p></body></html>");

      log.error(t.getMessage(), t);
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
