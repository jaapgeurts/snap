package snap.views;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import snap.http.RequestContext;

/**
 * A simple view that returns a String wrapped inside an HTML body. By default
 * it will return a HTTP 200 status code
 */
public class HtmlView extends View
{
  private int mStatus = HttpServletResponse.SC_OK;

  /**
   * Construct a HtmlView with the text to display in the body.
   *
   * @param text
   *          Text to put in the body.
   */
  public HtmlView(String text)
  {
    mText = text;
  }

  /**
   * Construct a HtmlView with the text to display in the body.
   *
   * @param text
   *          Text to put in the body
   * @param statusCode
   *          Http Status code to return.
   */
  public HtmlView(String text, int statusCode)
  {
    mText = text;
    mStatus = statusCode;
  }

  /**
   * Renders a plain HTML file with the string as the body. Not meant to be
   * called directly by the user
   */
  @Override
  public void render(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();

    r.setStatus(mStatus);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    ServletOutputStream os = r.getOutputStream();
    os.print(String.format("<html><head><title>StringView</title></head><body>%s</body></html>", mText));
  }

  private String mText;
}
