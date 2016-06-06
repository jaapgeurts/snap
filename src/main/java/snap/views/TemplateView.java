package snap.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.rythmengine.RythmEngine;
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

  /**
   * Gets the HTTP status code that will be set when this view is rendered
   * 
   * @return the HTTP status code
   */
  public int getStatusCode()
  {
    return mStatusCode;
  }

  /**
   * Sets the HTTP status code that will be set when this view is rendered
   * 
   * @param statusCode
   *          the HTTP status code
   */
  public void setStatusCode(int statusCode)
  {
    this.mStatusCode = statusCode;
  }

  /**
   * Gets the HTTP content type that will be set when this view is rendered
   * 
   * @return the HTTP content type header string
   */
  public String getContentType()
  {
    return mContentType;
  }

  /**
   * Sets the HTTP content type that will be set when this view is rendered
   * 
   * @param contentType
   *          the HTTP content type header string
   */
  public void setContentType(String contentType)
  {
    this.mContentType = contentType;
  }

  /**
   * Gets the HTTP character encoding that will be set when this view is
   * rendered
   * 
   * @return the HTTP encoding string
   */
  public String getCharEncoding()
  {
    return mCharEncoding;
  }

  /**
   * Sets the HTTP character encoding that will be set when this view is
   * rendered
   * 
   * @param charEncoding
   *          the HTTP encoding string
   */
  public void setCharEncoding(String charEncoding)
  {
    this.mCharEncoding = charEncoding;
  }

  /**
   * Renders the output to the outputstream.
   */
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

    r.setStatus(mStatusCode);
    r.setContentType(mContentType);
    r.setCharacterEncoding(mCharEncoding);

    RythmEngine engine = WebApplication.getInstance().getRenderEngine();
    Locale locale = context.getLocale();
    if (locale == null)
    {
      // get the locale from the servlet, which attempts to get it from the
      // Accept-Language Header or else the default system locale
      locale = context.getRequest().getLocale();
    }
    engine.prepare(locale);
    String s = engine.render(mTemplateName, mContext);
    ServletOutputStream os = r.getOutputStream();
    os.print(s);

  }

  protected String mTemplateName;
  protected Map<String, Object> mContext;

  private int mStatusCode = HttpServletResponse.SC_OK;
  private String mContentType = "text/html; charset=UTF-8";
  private String mCharEncoding = "UTF-8";

}
