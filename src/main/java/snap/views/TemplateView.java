package snap.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.User;
import snap.WebApplication;
import snap.http.RequestContext;

/**
 * This view renders an HTML template with data passed as parameters. This is
 * the most commonly used View to return for a request. To use this view call
 * its constructor with the name of a HTML template.
 * 
 * To pass pass data to the template you can add any object as a parameter to
 * this view by calling addParameter().
 * 
 * @author Jaap Geurts
 *
 */
public class TemplateView extends View
{
  final Logger log = LoggerFactory.getLogger(TemplateView.class);

  /**
   * Constructor.
   * 
   * @param templateName
   *          The filename of the template. The HTML template is relative to the
   *          src/main/webapp folder.
   */
  public TemplateView(String templateName)
  {
    mTemplateName = templateName;
    mContext = new HashMap<String, Object>();
  }

  /**
   * Adds a parameter to the template.
   * 
   * @param name
   *          The name by which the parameter will be referenced in the template
   * @param value
   *          The object to pass.
   */
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
    String tokenValue = context.getServerCsrfToken();
    if (tokenValue != null)
      mContext.put("csrf_token_value", tokenValue);

    // Add a user if they are logged in
    User user = context.getAuthenticatedUser();
    if (user != null)
      mContext.put("user", user);

    r.setStatus(HttpServletResponse.SC_OK);
    r.setContentType("text/html; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    String s = WebApplication.getInstance().getRenderEngine().render(mTemplateName, mContext);
    ServletOutputStream os = r.getOutputStream();
    os.print(s);

  }

  protected String mTemplateName;
  protected Map<String, Object> mContext;

}
