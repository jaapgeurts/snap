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

// TODO: make this a singleton
public class Router
{
  final static Logger log = LoggerFactory.getLogger(Router.class);

  public static Router getInstance()
  {
    if (mRouter == null)
      mRouter = new Router();
    return mRouter;
  }

  private Router()
  {
    mRouteList = new ArrayList<Route>();
    mRouteMap = new HashMap<String, Route>();

  }

  public void init(String contextPath) throws FileNotFoundException
  {
    mContextPath = contextPath;
    BufferedReader in = new BufferedReader(new InputStreamReader(getClass()
        .getClassLoader().getResourceAsStream((Settings.routesFile))));
    int i = 1;
    try
    {
      // todo: on failure attempt to read the next line
      while (in.ready())
      {
        String line = in.readLine();
        if (line.charAt(0) == '#')
          continue;
        String[] parts = line.split("\\s+");

        String alias = parts[2];
        Route route = null;
        if ("STATIC".equals(parts[0]))
        {
          route = new StaticRoute(mContextPath, parts[1], parts[2], parts[3]);
          mRouteList.add(route);
          mRouteMap.put(alias, route);
        }
        else
        {
          String[] methods = parts[0].split("\\,");
          if (methods != null && methods.length > 0 && methods[0].length() > 0
              && methods[0].charAt(0) == '*')
          {
            // for wildcard methods add a route for all methods
            // TODO: consider using a special Route named WildcardRoute
            // Or a route that can handle more than one method
            for (HttpMethod m : HttpMethod.values())
            {
              route = new Route(mContextPath, m, parts[1], alias, parts[3]);
              mRouteList.add(route);
              mRouteMap.put(alias, route);
            }
          }
          else
          {
            for (String methodName : methods)
            {
              try
              {
                route = new Route(mContextPath, HttpMethod.valueOf(methodName),
                    parts[1], alias, parts[3]);
                mRouteList.add(route);
                mRouteMap.put(alias, route);
              }
              catch (IllegalArgumentException iae)
              {
                log.error("Wrong method name \"" + methodName
                    + "\" for route: " + line);
              }
            }
          }
        }
        i++;
      }
      in.close();
      // res.close();
    }
    catch (IOException e)
    {
      log.error("An error happened during parsing route.conf. Line: " + i, e);
    }

  }

  /**
   * Finds a route in the list for the specified method and path
   * 
   * @param method
   * @param path
   * @return
   * @throws RouteNotFoundException
   *           when the method and path don't match any route rules
   */
  public Route findRouteForPath(HttpMethod method, String path)
  {
    for (Route route : mRouteList)
    {
      if (route.match(method, path))
        return route;
    }
    throw new RouteNotFoundException("Can't find route for Method: "
        + method.toString() + " path: " + path);
  }

  public String siteUrl()
  {
    return Settings.siteRootUrl;
  }

  public HttpRedirect redirectForRoute(String alias, Object... params)
  {
    return new HttpRedirect(linkForRoute(alias, params));
  }

  public HttpRedirect redirectForRoute(String alias,
      Map<String, String> getParams, Object... params)
  {
    return new HttpRedirect(linkForRoute(alias, getParams, params));
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
    Route route = getRoute(alias);
    return route.getLink(params);
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
  public String linkForRoute(String alias, Map<String, String> getParams,
      Object... params)
  {
    Route route = getRoute(alias);
    return route.getLink(getParams, params);
  }

  /**
   * 
   * @param routeAlias
   * @return
   * @throws RouteNotFoundException
   *           When the route can't be found.
   */
  public Route getRoute(String routeAlias)
  {
    Route route = mRouteMap.get(routeAlias);
    if (route == null)
      throw new RouteNotFoundException("Can't redirect: Unknown route: "
          + routeAlias);
    return route;
  }

  public void setContextPath(String contextPath)
  {
    mContextPath = contextPath;
  }

  private ArrayList<Route> mRouteList;
  private HashMap<String, Route> mRouteMap;
  private String mContextPath;

  private static Router mRouter = null;

}
