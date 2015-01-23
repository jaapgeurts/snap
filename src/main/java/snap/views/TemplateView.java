package snap.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.User;
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

    // add csrf_token if the user is logged in
    String tokenValue = context.getCsrfToken();
    if (tokenValue != null)
      mContext.put("csrf_token_value", tokenValue);

    // Add a user if they are logged in
    User user = context.getAuthenticatedUser();
    if (user != null)
      mContext.put("user", user);

    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    String s = WebApplication.getInstance().getRenderEngine()
        .render(mTemplateName, mContext);
    PrintWriter writer = r.getWriter();
    writer.print(s);

  }

  protected String mTemplateName;
  protected Map<String, Object> mContext;

}
