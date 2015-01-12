package snap.views;

import java.io.IOException;

import snap.http.HttpResponse;

public interface View
{
  public void render(HttpResponse response) throws RenderException, IOException;

}
