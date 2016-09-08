package snap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.HttpMethod;
import snap.http.HttpRedirect;
import snap.http.RedirectType;

public class Router
{
  final static Logger log = LoggerFactory.getLogger(Router.class);

  /**
   * Get the router instance of this application.
   *
   * @return The Router singleton instance
   */
  public static Router getInstance()
  {
    if (mRouter == null)
      mRouter = new Router();
    return mRouter;
  }

  /**
   * Construct a new Router. Users should NOT call this. They should call the
   * Static method.
   */
  private Router()
  {
    mRouteList = new ArrayList<>();
    mRouteMap = new HashMap<>();
  }

  /**
   * Initialize the router with the Servlet Container Context Path
   *
   * @param contextPath
   *          - The path under which this servlet runs and this router is
   *          decoding url paths
   * @throws FileNotFoundException
   *           Thrown if the route can't find the route file
   */
  public void init(String contextPath) throws FileNotFoundException
  {
    mContextPath = contextPath;
    BufferedReader in = new BufferedReader(
        new InputStreamReader(getClass().getClassLoader().getResourceAsStream((Settings.routesFile))));
    int i = 1;
    try
    {
      // todo: on failure attempt to read the next line
      while (in.ready())
      {
        String line = in.readLine();
        // comment line: skip it.
        if (line.isEmpty() || line.charAt(0) == '#')
          continue;

        String[] parts = line.split("\\s+");

        String alias = parts[2];
        Route route = null;

        try
        {
          if ("ACTION".equals(parts[0]))
            route = new ActionRoute();
          else if ("STATIC".equals(parts[0]))
            route = new StaticRoute();
          else if ("CUSTOM".equals(parts[0]))
          {
            String className = parts[3];
            if (className.charAt(0) == '.')
              className = Settings.packagePrefix + className;

            route = (Route)Class.forName(className).newInstance();
          }
          if (route != null)
          {
            route.init(mContextPath, alias, parts[1], parts[3]);
            RouteMatcher routeMatcher = new RouteMatcher(contextPath, alias, parts[1], route);
            mRouteList.add(routeMatcher);
            mRouteMap.put(alias, routeMatcher);
          }
        }
        catch (SnapException | InstantiationException | IllegalAccessException | ClassNotFoundException e)
        {
          log.warn("Error creating route " + alias, e);
        }

        i++;
      }
      in.close();
      // res.close();
    }
    catch (

    IOException e)
    {
      log.error("An error happened during parsing route.conf at line: " + i, e);
    }

  }

  /**
   * Finds a route in the list for the specified method and path. Returns the
   * first match
   *
   * @param method
   *          The HTTP method
   * @param path
   *          The requested URL path
   * @return the route matcher that matched the method and path
   * @throws RouteNotFoundException
   *           when the method and path don't match any route rules
   * @throws HttpMethodException
   *           when a rule was found but with an incorrect http method
   */
  public RouteMatcher findRouteMatcherForPath(HttpMethod method, String path)
  {
    return internalFindRouteMatcherForPath(method, path, 0);
  }

  /**
   * Finds a route in the list for the method and path, but starts at rule after
   * the rule of the matcher.
   *
   * @param method
   *          the method for which to find a path
   * @param path
   *          the path as sent by the http client
   * @param matcher
   *          the current matcher
   * @return the route matcher found or throw exception if not found
   */
  public RouteMatcher findNextRouteMatcherForPath(HttpMethod method, String path, RouteMatcher matcher)
  {
    // find the current matcher.
    int start = -1;
    for (int i = 0; i < mRouteList.size(); i++)
    {
      RouteMatcher m = mRouteList.get(i);
      if (m.equals(matcher))
      {
        start = i + 1;
        break;
      }
    }
    if (start == -1)
      throw new SnapException("Route matcher not found for matcher: " + matcher.toString());
    return internalFindRouteMatcherForPath(method, path, start);
  }

  private RouteMatcher internalFindRouteMatcherForPath(HttpMethod method, String path, int start)
  {
    RouteMatcher foundRouteMatcher = null;
    for (int i = start; i < mRouteList.size(); i++)
    {
      RouteMatcher matcher = mRouteList.get(i);
      if (matcher.match(path))
      {
        foundRouteMatcher = matcher;
        break;
      }
    }
    if (foundRouteMatcher == null)
      throw new RouteNotFoundException(
          "Can't find route for Method: " + method.toString() + " path: " + path);

    HttpMethod[] methods = foundRouteMatcher.getRoute().getHttpMethods();
    if (methods == null)
      throw new SnapException("Route '" + foundRouteMatcher.getAlias() + "' does not allow any HTTP methods");

    for (HttpMethod m : methods)
      if (method == m)
        return foundRouteMatcher;

    throw new HttpMethodException("Route " + foundRouteMatcher.getAlias() + " matches path " + path
        + ", but has incorrect method " + method.toString());
  }

  /**
   * Get the site base url as defined in the snap.properties resource
   *
   * @return the URl as a string
   */
  public String siteUrl()
  {
    return Settings.siteRootUrl;
  }

  /**
   * Returns a redirect object for the specified route.
   *
   * @param alias
   *          The Route alias
   * @param type
   *          The redirect type
   * @param params
   *          The params to replace in the URL of the route.
   * @return The HttpRedirect object which can be returned as a RequestResult in
   *         the controller method
   */
  public HttpRedirect redirectForRoute(String alias, RedirectType type, Object... params)
  {
    return new HttpRedirect(linkForRoute(alias, params), type);
  }

  /**
   * Returns a redirect object for the specified route.
   *
   * @param alias
   *          The Route alias
   * @param type
   *          The redirect type
   * @param getParams
   *          The query string params to append to the URL
   * @param params
   *          The params to replace in the URL of the route.
   * @return The HttpRedirect object which can be returned as a RequestResult in
   *         the controller method
   */
  public HttpRedirect redirectForRoute(String alias, RedirectType type, Map<String, Object> getParams,
      Object... params)
  {
    return new HttpRedirect(linkForRoute(alias, getParams, params), type);
  }

  /**
   * Returns the URL string for a route with name alias. Any regex pattern
   * groups will be substituted with the values in the params list*
   *
   * @param alias
   *          The alias name of the route
   * @param params
   *          The params the be replaces in the Regex groups
   * @return The Url link as a string. If no route found returns "";
   * @throws RouteNotFoundException
   *           When the route can't be found.
   */
  public String linkForRoute(String alias, Object... params)
  {
    RouteMatcher routeMatcher = getRouteMatcher(alias);
    return routeMatcher.getLink(params);
  }

  /***
   * Returns the URL string for a route with name alias. Any regex pattern
   * groups will be substituted with the values in the params list*
   *
   * @param alias
   *          The alias name of the route
   * @param getParams
   *          The params to be appended to the URL
   * @param params
   *          The params the be replaces in the Regex groups
   * @return The Url link as a string
   * @throws RouteNotFoundException
   *           When the route can't be found.
   */
  public String linkForRoute(String alias, Map<String, Object> getParams, Object... params)
  {
    RouteMatcher routeMatcher = getRouteMatcher(alias);
    return routeMatcher.getLink(getParams, params);
  }

  /**
   *
   * @param routeAlias
   *          The name by which this route is known
   * @return The route for this alias
   * @throws RouteNotFoundException
   *           When the route can't be found.
   */
  public Route getRoute(String routeAlias)
  {
    RouteMatcher route = mRouteMap.get(routeAlias);
    if (route == null)
      throw new RouteNotFoundException("Can't redirect: Unknown route: " + routeAlias);
    return route.getRoute();
  }

  /**
   * Returns the route matcher for a specific route alias
   *
   * @param routeAlias
   *          the name by which this route is known
   * @return The matcher
   */
  private RouteMatcher getRouteMatcher(String routeAlias)
  {
    RouteMatcher routeMatcher = mRouteMap.get(routeAlias);
    if (routeMatcher == null)
      throw new RouteNotFoundException("Can't find route matcher for alias: " + routeAlias);
    return routeMatcher;
  }

  /**
   * Sets the context path of this router as defined by the web.xml file. Not
   * meant to be called by users.
   *
   * @param contextPath
   *          The root context path
   */
  public void setContextPath(String contextPath)
  {
    mContextPath = contextPath;
  }

  private ArrayList<RouteMatcher> mRouteList;
  private HashMap<String, RouteMatcher> mRouteMap;
  private String mContextPath;

  private static Router mRouter = null;

}
