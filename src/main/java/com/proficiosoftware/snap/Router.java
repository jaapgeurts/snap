package com.proficiosoftware.snap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: make this a singleton
public class Router
{
  final static Logger log = LoggerFactory.getLogger(Router.class);

  public static Router instance()
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
          for (String method : methods)
          {
            route = new Route(mContextPath, method, parts[1], alias, parts[3]);
            mRouteList.add(route);
            mRouteMap.put(alias, route);
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

  public Route findRouteForPath(String method, String path)
  {
    for (Route route : mRouteList)
    {
      if (route.match(method, path))
        return route;
    }
    return null;
  }

  public String linkForRoute(String alias, Object... params)
  {
    Route route = mRouteMap.get(alias);
    if (route != null)
      return route.getLink(params);
    return "";
  }

  public Route getRoute(String routeAlias)
  {
    return mRouteMap.get(routeAlias);
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
