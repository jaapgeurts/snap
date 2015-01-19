package snap;

import snap.http.RequestContext;
import snap.http.RequestResult;
import snap.views.View;

public interface Controller
{
  public RequestResult handleRequest(RequestContext context);
}
