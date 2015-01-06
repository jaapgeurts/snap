package com.proficiosoftware.snap.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.proficiosoftware.snap.http.HttpResponse;

public class JsonView implements View
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
  public void render(HttpResponse response) throws RenderException, IOException
  {
    HttpServletResponse r = response.getResponse();
    PrintWriter pw = r.getWriter();
    pw.print(mJson.toJSONString());
    // TODO: specify encoding too
    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("application/json");
  }

  private JSONObject mJson;

}
