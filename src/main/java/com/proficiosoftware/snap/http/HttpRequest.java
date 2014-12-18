package com.proficiosoftware.snap.http;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.proficiosoftware.snap.Route;

public class HttpRequest
{
  public static final String HTTP_GET = "GET";
  public static final String HTTP_POST = "POST";

  public HttpRequest(Route route, HttpServletRequest servletRequest,
      String method)
  {
    mRoute = route;
    mParams = new HashMap<String, String[]>();
    mParams.putAll(servletRequest.getParameterMap());
    mServletRequest = servletRequest;
    mMethod = method;

    mAuthorizedUser = getRequest().getSession().getAttribute(
        "Snap.AuthorizedUser");
  }

  public Map<String, String[]> getParams()
  {
    return mParams;
  }

  /**
   * Return variable with name. implicitly returns first value in the list
   * 
   * @param name
   * @return returns null when parameter is not avialable.
   */
  public String getParam(String name)
  {
    return getParam(name, 0);
  }

  /**
   * Return variable with name and in position pos of the list
   * 
   * @param name
   *          the name of the variable
   * @param pos
   *          index of the value in the parameter list
   * @return Returns null when parameter is not available
   */

  public String getParam(String name, int pos)
  {
    String[] list = mParams.get(name);
    if (list != null)
      return list[pos];
    return null;
  }

  public String getMethod()
  {
    return mMethod;
  }

  void addParameter(String key, String value)
  {
    String[] values = mParams.get(key);
    if (values == null)
    {
      mParams.put(key, new String[] { value });
    }
    else
    {
      values = new String[values.length + 1];
      values[values.length - 1] = value;
      mParams.put(key, values);
    }
  }

  public void addParameters(Map<String, String[]> params)
  {
    if (params == null)
      return;
    // TODO: check for existing parameters
    mParams.putAll(params);
  }

  public HttpServletRequest getRequest()
  {
    return mServletRequest;
  }

  public Route getRoute()
  {
    return mRoute;
  }

  public Object getAuthorizedUser()
  {
    return mAuthorizedUser;
  }

  public void setAuthorizedUser(Object user)
  {
    getRequest().getSession().setAttribute("Snap.AuthorizedUser", user);
    mAuthorizedUser = user;
  }

  private final Map<String, String[]> mParams;

  private HttpServletRequest mServletRequest;
  private String mMethod;
  private Route mRoute;
  private Object mAuthorizedUser;

}
