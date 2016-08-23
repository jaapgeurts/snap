package snap.http;

import java.io.IOException;

/**
 * Expected result from a router method. You can either return a View or a
 * Redirect. In most situations you will want to return a TemplateView or a
 * NullView. Other available views are JsonView and StringView.
 *
 * If you need to redirect the user to another URL. call getRedirect() or
 * getRedirectSelf() on the RequestContex to generate a redirect object which
 * you should return in the router method.
 *
 * @author Jaap Geurts
 *
 */
public interface RequestResult
{
  /**
   * This method is called by the framework the response output. You should
   * normally not implement this interface but instead extend from View and
   * implement the render() method.
   *
   * @param context
   *          The request context
   * @throws IOException
   *           thrown when an error occurs
   */
  void handleResult(RequestContext context) throws IOException;
}
