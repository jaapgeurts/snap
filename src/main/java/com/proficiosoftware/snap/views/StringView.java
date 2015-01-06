package com.proficiosoftware.snap.views;

import java.io.IOException;
import java.io.PrintWriter;

import com.proficiosoftware.snap.http.HttpResponse;

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
    PrintWriter pw = response.getResponse().getWriter();
    pw.print(String.format(
        "<html><head><title>StringView</title></head><body>%s</body></html>",
        mText));
  }

  private String mText;
}
