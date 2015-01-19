package snap.http;

import java.io.IOException;

public interface RequestResult
{
  public void handleResult(RequestContext context) throws IOException;
}
