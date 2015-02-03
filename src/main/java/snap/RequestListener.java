package snap;

import snap.http.RequestContext;

public interface RequestListener
{
  /**
   * Perform processing before a route is processed
   * 
   * @param context
   *          The Request Context.
   * @return
   */
  public void onBeforeRequest(RequestContext context)
      throws AuthenticationException, AuthorizationException;

  /**
   * Perform processing after a route is processed
   * 
   * @param context
   *          The Request Context.
   * @return
   */
  public void onAfterRequest(RequestContext context);
}
