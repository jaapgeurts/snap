package snap.views;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

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

  private Integer mStatus = null;

  /**
   * A view that doesn't change anything about the response
   */
  public NullView()
  {
  }

  /**
   * A View that only changes the status code of the response
   *
   * @param statusCode
   *          The status code to return
   */
  public NullView(int statusCode)
  {
    mStatus = statusCode;
  }

  @Override
  public void render(RequestContext context) throws IOException
  {
    if (mStatus != null)
    {
      HttpServletResponse r = context.getResponse();
      r.setStatus(mStatus);
    }
  }

}
