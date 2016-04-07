package snap.views;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import snap.http.RequestContext;

public class StringView extends View
{

  public StringView(String text)
  {
    mText = text;
  }

  /**
   * Renders a plain HTML file with the string as the body.
   */
  @Override
  public void render(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();

    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    ServletOutputStream os = r.getOutputStream();
    os.print(String.format("<html><head><title>StringView</title></head><body>%s</body></html>", mText));
  }

  private String mText;
}
