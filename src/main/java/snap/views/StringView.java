package snap.views;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import snap.http.HttpResponse;

public class StringView implements View
{

  public StringView(String text)
  {
    mText = text;
  }

  /**
   * Renders a plain HTML file with the string as the body.
   */
  @Override
  public void render(HttpResponse response) throws RenderException, IOException
  {
    HttpServletResponse r = response.getResponse();
    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("application/json; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    PrintWriter pw = response.getResponse().getWriter();
    pw.print(String.format(
        "<html><head><title>StringView</title></head><body>%s</body></html>",
        mText));
  }

  private String mText;
}
