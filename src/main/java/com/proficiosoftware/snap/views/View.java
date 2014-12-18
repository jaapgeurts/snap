package com.proficiosoftware.snap.views;

import java.io.IOException;
import com.proficiosoftware.snap.http.HttpResponse;

public interface View
{
  public void render(HttpResponse response) throws RenderException, IOException;

}
