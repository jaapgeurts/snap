package snap;

import snap.http.RequestContext;
import snap.http.RequestResult;

public interface RouteListener
{

  /**
   * Perform processing before a route is processed
   * 
   * @param context
   *          The Request Context.
   * @return null if you want to process the current route. A valid
   *         RequestResult if you want to skip processing the current
   *         route(onAfterRoute will not be called)
   */
  public RequestResult onBeforeRoute(RequestContext context)
      throws AuthenticationException, AuthorizationException;

  /**
   * Perform processing after a route is processed
   * 
   * @param context
   *          The Request Context.
   * @return
   */
  public void onAfterRoute(RequestContext context);
}
