package snap.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.WebApplication;
import snap.http.RequestContext;

public class TemplateView extends View
{
  final Logger log = LoggerFactory.getLogger(TemplateView.class);

  public TemplateView(String templateName)
  {
    mTemplateName = templateName;
    mContext = new HashMap<String, Object>();
  }

  public void addParameter(String name, Object value)
  {
    mContext.put(name, value);
  }

  @Override
  public void render(RequestContext context) throws IOException
  {

    HttpServletResponse r = context.getResponse();
    mContext.put("router", context.getRouter());

    String s = WebApplication.getInstance().getRenderEngine()
        .render(mTemplateName, mContext);
    PrintWriter writer = r.getWriter();
    writer.print(s);

  }

  protected String mTemplateName;
  protected Map<String, Object> mContext;

}
