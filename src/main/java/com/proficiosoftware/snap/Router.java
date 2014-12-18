package com.proficiosoftware.snap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    {
      try
      {
        mRouter = new Router();
      }
      catch (FileNotFoundException e)
      {
        log.error("Can't open router config file.", e);
        return null;
      }
    }
    return mRouter;
  }

  private Router() throws FileNotFoundException
  {
    mRouteList = new ArrayList<Route>();
    mRouteMap = new HashMap<String, Route>();

    // TODO: find routes file location from properties
    // BufferedReader in = new BufferedReader(new FileReader(routesFile));
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
          route = new StaticRoute(parts[1], parts[2], parts[3]);
        }
        else
        {
          route = new Route(parts[0], parts[1], alias, parts[3]);
        }
        mRouteList.add(route);
        mRouteMap.put(alias, route);
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

  private ArrayList<Route> mRouteList;
  private HashMap<String, Route> mRouteMap;

  private static Router mRouter = null;

}
