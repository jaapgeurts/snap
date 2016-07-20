package snap.views;

import java.io.IOException;

import snap.http.RequestContext;

/**
 * This View is a view that does nothing. You can return the static instance
 * variable INSTANCE when you need don't want to change the response. This is
 * useful when you want to set a custom response on the servlet
 * HttpServletResponse
 *
 * @author Jaap Geurts
 *
 */
public class NullView extends View
{

  /**
   * A convenience instance variable
   *
   */
  public static NullView INSTANCE = new NullView();

  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // do nothing
  }

  @Override
  public void render(RequestContext context) throws IOException
  {
    // do nothing
  }

}
