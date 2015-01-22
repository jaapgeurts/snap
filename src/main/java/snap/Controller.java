package snap;

import snap.http.RequestContext;
import snap.http.RequestResult;

public interface Controller
{
  public RequestResult handleRequest(RequestContext context);
}
