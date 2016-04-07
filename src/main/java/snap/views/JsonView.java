package snap.views;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import snap.http.RequestContext;

import com.alibaba.fastjson.JSON;

public class JsonView extends View
{

  private int mStatus = HttpServletResponse.SC_OK;

  public JsonView(JSON root)
  {
    if (root == null)
      throw new NullPointerException("Argument \"root\" can't be NULL");
    mJson = root;
  }

  public JsonView(JSON root, int status)
  {
    this(root);
    mStatus = status;
  }

  @Override
  public void render(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();

    r.setStatus(mStatus);
    r.setContentType("application/json; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    ServletOutputStream os = r.getOutputStream();
    if (mJson != null)
      os.print(mJson.toJSONString());
    // TODO: specify encoding too

  }

  private JSON mJson;

}
