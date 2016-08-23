package snap;

import snap.http.RequestContext;
import snap.http.RequestResult;

public interface Controller
{
  RequestResult handleRequest(RequestContext context);
}
