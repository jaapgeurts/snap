package snap.views;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import snap.WebApplication;
import snap.http.RequestContext;

/**
 * The default view for returning JSON content. The default status code is HTTP
 * OK 200.
 *
 * This view will use the JsonMapper supplied by WebApplication.getJsonMapper();
 * By default this is Jackson
 *
 * @author Jaap Geurts
 *
 * @param <T>
 *          The type of object to serialize to JSON
 */
public class JsonView<T> extends View
{

  private int mStatus = HttpServletResponse.SC_OK;

  /**
   * Construct a JsonView with the root object to serialize.
   *
   * @param root
   *          The object to serialize.
   */
  public JsonView(T root)
  {
    if (root == null)
      throw new IllegalArgumentException("Root object for JSON can't be null");
    mRoot = root;
    mapper = WebApplication.getInstance().getJsonMapper();
  }

  /**
   * Construct a Json View
   *
   * @param root
   *          The root object to serialize.
   * @param status
   *          The HTTP status code to return
   */
  public JsonView(T root, int status)
  {
    this(root);
    mStatus = status;
  }

  /**
   * Renders the output to the servlet output stream. Not meant to be called by
   * end users
   */
  @Override
  public void render(RequestContext context) throws IOException
  {
    HttpServletResponse r = context.getResponse();

    r.setStatus(mStatus);
    r.setContentType("application/json; charset=UTF-8");
    r.setCharacterEncoding("UTF-8");

    ServletOutputStream os = r.getOutputStream();

    mapper.writeValue(os, mRoot);
  }

  private T mRoot;
  private ObjectMapper mapper;
}
