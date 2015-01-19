package snap;

import snap.http.RequestContext;
import snap.views.View;

public interface Controller
{
  public View handleRequest(RequestContext context);
}
