package snap.views;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import snap.WebApplication;
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

    PrintWriter pw = r.getWriter();
    pw.print(String.format(
        "<html><head><title>StringView</title></head><body>%s</body></html>",
        mText));
  }

  private String mText;
}
