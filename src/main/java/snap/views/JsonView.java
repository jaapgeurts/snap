package snap.views;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import snap.http.RequestContext;

import com.alibaba.fastjson.JSONObject;

public class JsonView extends View
{

  public JsonView()
  {
    mJson = new JSONObject();
  }

  public JsonView(JSONObject root)
  {
    mJson = root;
  }

  @Override
  public void render(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();
    PrintWriter pw = r.getWriter();
    pw.print(mJson.toJSONString());
    // TODO: specify encoding too

  }

  private JSONObject mJson;

}
