package snap.views;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import snap.http.RequestContext;

/**
 * A simple view that returns a string as the body
 */
public class StringView extends View
{
  private int mStatus = HttpServletResponse.SC_OK;

  public StringView(String text)
  {
    mText = text;
  }

  public StringView(String text, int statusCode)
  {
    mText = text;
    mStatus = statusCode;
  }

  /**
   * Renders a plain HTML file with the string as the body. Not meant to be
   * called directly by the user.
   */
  @Override
  public void render(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();

    r.setStatus(mStatus);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    ServletOutputStream os = r.getOutputStream();
    os.print(mText);
  }

  private String mText;
}
