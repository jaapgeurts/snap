package com.proficiosoftware.snap.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class View
{
  final Logger log = LoggerFactory.getLogger(View.class);

  private static final String NO_VIEW_DEFINED = "No view has been defined for this action";

  public CharSequence render() throws RenderException
  {
    return NO_VIEW_DEFINED;
  }

  public void render(OutputStream os)
  {
    try
    {
      os.write(NO_VIEW_DEFINED.getBytes());
    }
    catch (IOException e)
    {
      // TODO: log exception here
      e.printStackTrace();
    }
  }

  public void render(PrintStream ps)
  {
    ps.print(NO_VIEW_DEFINED);
  }

}
