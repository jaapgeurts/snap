package snap;

import snap.http.HttpRequest;
import snap.http.HttpResponse;
import snap.views.View;

public interface Controller
{
  public View handleRequest(HttpRequest request, HttpResponse response);
}
