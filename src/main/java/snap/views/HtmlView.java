package snap.views;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import snap.http.RequestContext;

/**
 * A simple view that returns a String wrapped inside an HTML body.
 */
public class HtmlView extends View
{
  private int mStatus = HttpServletResponse.SC_OK;

  public HtmlView(String text)
  {
    mText = text;
  }

  public HtmlView(String text, int statusCode)
  {
    mText = text;
    mStatus = statusCode;
  }

  /**
   * Renders a plain HTML file with the string as the body.
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
