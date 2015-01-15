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

import snap.Router;
import snap.WebApplication;
import snap.http.HttpResponse;

public class TemplateView implements View
{
  final Logger log = LoggerFactory.getLogger(TemplateView.class);

  public TemplateView(String templateName)
  {
    mTemplateName = templateName;
    mContext = new HashMap<String, Object>();
    mContext.put("router", Router.instance());
  }

  public void addParameter(String name, Object value)
  {
    mContext.put(name, value);
  }

  @Override
  public void render(HttpResponse response) throws RenderException, IOException
  {
    // TODO: consider adding the User object automatically

    String template = mTemplateName;

    // ServletContext context = WebApplication.Instance().getContext();

    // log.debug("Path: "+context.getRealPath("."));
    // InputStream is = context.getResourceAsStream("/"+mTemplateName);
    // template = StreamToString(is);

    HttpServletResponse r = response.getResponse();
    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    PrintWriter pw = r.getWriter();
    pw.print(WebApplication.getInstance().getRenderEngine()
        .render(template, mContext));

  }

  protected String StreamToString(InputStream in) throws RenderException
  {
    if (in == null)
    {
      log.warn("Error reading snap error template");
      throw new RenderException("Can't read template: " + mTemplateName);
    }
    BufferedReader br;
    StringBuilder builder = new StringBuilder();
    try
    {
      br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

      char[] buffer = new char[2048];
      int len;
      len = br.read(buffer);
      while (len != -1)
      {
        builder.append(buffer, 0, len);
        len = br.read(buffer);
      }
      return builder.toString();
    }
    catch (UnsupportedEncodingException e)
    {
      log.warn("JVM doesn't support UTF-8 encoding", e);
      throw new RenderException("Can't read template: " + mTemplateName, e);
    }
    catch (IOException e)
    {
      log.warn("Error reading template", e);
      throw new RenderException("Can't read template: " + mTemplateName, e);
    }
  }

  protected String mTemplateName;
  protected Map<String, Object> mContext;

}
