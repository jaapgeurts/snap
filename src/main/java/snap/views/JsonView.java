package snap.views;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import snap.http.RequestContext;

import com.alibaba.fastjson.JSON;

public class JsonView extends View
{


  public JsonView(JSON root)
  {
    if (root == null)
      throw new NullPointerException("Argument \"root\" can't be NULL");
    mJson = root;
  }

  @Override
  public void render(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();

    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("application/json; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    PrintWriter pw = r.getWriter();
    if (mJson != null)
      pw.print(mJson.toJSONString());
    // TODO: specify encoding too

  }

  private JSON mJson;

}
