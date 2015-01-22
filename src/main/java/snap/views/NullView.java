package snap.views;

import java.io.IOException;

import snap.http.RequestContext;

public class NullView extends View
{

  public static NullView INSTANCE = new NullView();

  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // do nothing
  }

  @Override
  public void render(RequestContext context) throws IOException
  {
    // do nothing
  }

}
