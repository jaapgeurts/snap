package snap.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    WebApplication.getInstance().getRenderEngine()
        .render(r.getOutputStream(), mTemplateName, mContext);

  }

  protected String StreamToString(InputStream in) throws IOException
  {
    if (in == null)
    {
      log.warn("Inputstream argument can't be null");
      throw new IllegalArgumentException("Can't read template: "
          + mTemplateName);
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
      log.error("JVM doesn't support UTF-8 encoding: template: "
          + mTemplateName, e);
      throw e;
    }
    catch (IOException e)
    {
      log.error("IO Exception reading template: " + mTemplateName, e);
      throw e;
    }
  }

  protected String mTemplateName;
  protected Map<String, Object> mContext;

}
