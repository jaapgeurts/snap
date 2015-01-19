package snap.http;

import java.io.IOException;

public class HttpNull implements RequestResult
{

  public static HttpNull INSTANCE = new HttpNull();

  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // do nothing
  }

}
