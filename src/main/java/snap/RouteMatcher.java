package snap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.NameEntry;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.HttpMethod;
import snap.http.HttpRedirect;
import snap.http.RedirectType;
import snap.http.RequestContext;
import snap.http.RequestResult;

public class RouteMatcher
{
  private final Logger log = LoggerFactory.getLogger(RouteMatcher.class);

  public RouteMatcher(String contextPath, String alias, String url, Route route)
  {
    mServletContextPath = contextPath;
    mPath = url;
    mAlias = alias;

    byte[] re = mPath.getBytes();
    mRegex = new Regex(re, 0, re.length, Option.NONE, UTF8Encoding.INSTANCE);

    mRoute = route;

  }

  /**
   * Returns true if the urlPath matches this route's rule and method
   *
   * @param method
   *          The HTTP method
   * @param urlPath
   *          the URL path to match
   * @return true if this method and path match
   */
  public boolean match(HttpMethod method, String urlPath)
  {
    boolean found = false;
    for (HttpMethod m : mRoute.getHttpMethods())
    {
      if (m == method)
      {
        found = true;
        break;
      }
    }
    if (!found)
      return false;

    boolean success = false;
    byte[] p = urlPath.getBytes();
    Matcher m = mRegex.matcher(p);
    success = m.search(0, p.length, Option.DEFAULT) != -1;

    return success;
  }

  /**
   * Returns true if the urlPath matches this route's rule
   *
   * @param urlPath
   *          the path to match
   * @return true if matched, false otherwise
   */
  public boolean match(String urlPath)
  {
    boolean success = false;
    byte[] p = urlPath.getBytes();
    Matcher m = mRegex.matcher(p);
    success = m.search(0, p.length, Option.DEFAULT) != -1;

    return success;
  }

  public RequestResult handleRoute(RequestContext context) throws Throwable
  {
    return getRoute().handleRoute(context);
  }

  /**
   * Reverse a link for this route. Get a link that you can use in HTML for this
   * route. It is assumed that this link has no replaceable groups.
   *
   * @return a relative URL as a string. (Excludes Protocol, host, port)
   */
  public String getLink()
  {
    return getLink(null, null);
  }

  /**
   * Reverse a link for this route. Get a link that you can use in HTML for this
   * route. Pass any groups that need to be replaced as an Object array. To get
   * the replacement value this method will call .toString() on each Object.
   *
   * @param params
   *          The params to replace in the groups
   * @return a relative URL as a string. (Excludes Protocol, host, port)
   */
  public String getLink(Object[] params)
  {
    return getLink(null, params);
  }

  /**
   * Reverse a link for this route. Get a link that you can use in HTML for this
   * route. Pass any groups that need to be replaced as an Object array in
   * params. To get the replacement value this method will call .toString() on
   * each Object.
   *
   * @param params
   *          The params to replace in the groups.
   * @param getParams.
   *          The params to append to the end of the URL as key value pairs (The
   *          part after the ?).
   * @return a relative URL as a string. (Excludes Protocol, host, port)
   */
  public String getLink(Map<String, Object> getParams, Object[] params)
  {
    StringBuilder builder = new StringBuilder();
    java.util.regex.Pattern pat = Pattern.compile("\\(.+?\\)");
    java.util.regex.Matcher m = pat.matcher(mPath);
    int regExLength = mPath.length();
    int start = 0;
    if (mPath.charAt(0) == '^')
      start++;
    int i = 0;
    // TODO: check for missing parameters and report
    while (m.find(start))
    {
      try
      {
        builder.append(mPath.substring(start, m.start()));
        if (i < params.length)
        {
          builder.append(URLEncoder.encode(params[i].toString(), "UTF-8"));
        }
        else
        {
          String message = "Not enough parameters when reversing link: " + mPath;
          log.error(message);
          throw new SnapException(message);
        }
        i++;
        start = m.end();
      }
      catch (UnsupportedEncodingException e)
      {
        log.debug("JVM doesn't support UTF-8", e);
      }
    }
    if (mPath.charAt(regExLength - 1) == '$')
    {
      regExLength--;
      if (start != regExLength)
      {
        builder.append(mPath.substring(start, regExLength));
      }
    }

    // add get params if available
    if (getParams != null && getParams.size() > 0)
    {
      builder.append("?");
      String queryString = getParams.entrySet().stream().map(e -> {
        try
        {
          return URLEncoder.encode(e.getKey(), "UTF-8") + "="
              + URLEncoder.encode(e.getValue().toString(), "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
          log.debug("JVM doesn't support UTF-8", ex);
          throw new UnsupportedOperationException("JVM doesn't support UTF-8", ex);
        }
      }).collect(Collectors.joining("&"));
      builder.append(queryString);
    }

    String finalLink;
    if (mServletContextPath == null || "".equals(mServletContextPath))
      finalLink = builder.toString();
    else
      finalLink = mServletContextPath + builder.toString();

    return mRoute.getLink(finalLink, getParams, params);
  }

  public HttpRedirect getRedirect(Object[] params, RedirectType type)
  {
    return new HttpRedirect(getLink(params), type);
  }

  /**
   * Return all the parameters that appear in the URL for this route
   *
   * @param path
   *          the Path to decode
   * @return A map of decoded parameters
   */
  public Map<String, String> getParameters(String path)
  {
    if (path == null || "".equals(path))
      return null;

    HashMap<String, String> map = new HashMap<>();
    byte[] p = path.getBytes();
    Matcher m = mRegex.matcher(p);
    int result = m.search(0, p.length, Option.CAPTURE_GROUP);
    if (result == -1)
      return null;

    // TODO: check the regex group ( if it's of digit class then convert to
    // Long)
    if (result >= 0 && mRegex.numberOfNames() > 0)
    {
      Region region = m.getEagerRegion();
      Iterator<NameEntry> entries = mRegex.namedBackrefIterator();
      while (entries.hasNext())
      {
        NameEntry entry = entries.next();
        String name = new String(entry.name, entry.nameP, entry.nameEnd - entry.nameP);
        int number = entry.getBackRefs()[0];
        int start = region.beg[number];
        int end = region.end[number];
        String value = new String(p, start, end - start, UTF8Encoding.INSTANCE.getCharset());
        map.put(name, value);
      }
    }
    return map;
  }

  /**
   * Get the alias by which this route is known.
   *
   * @return the alias
   */
  public String getAlias()
  {
    return mAlias;
  }

  /**
   * Get the URL path of this route as found in the routes.conf file
   *
   * @return the path of this route
   */
  public String getPath()
  {
    return mPath;
  }

  /**
   * Returns the context path under which this app is running in the servlet
   * container
   *
   * @return the path as a string.
   */
  public String getContextPath()
  {
    return mServletContextPath;
  }

  /**
   * Return the route for this matcher
   *
   * @return the route
   */
  public Route getRoute()
  {
    return mRoute;
  }

  private String mServletContextPath;
  private String mPath;
  private String mAlias;
  private Regex mRegex;

  private Route mRoute;

}