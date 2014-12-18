package com.proficiosoftware.snap.views;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class View
{
  private static final String NO_VIEW_DEFINED = "No view has been defined for this action";

  public CharSequence render()
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
