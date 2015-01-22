package snap.views;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.RequestContext;
import snap.http.RequestResult;

public abstract class View implements RequestResult
{
  final Logger log = LoggerFactory.getLogger(View.class);

  public void handleResult(RequestContext context) throws IOException
  {
    render(context);
  }

  public abstract void render(RequestContext context) throws IOException;

}
