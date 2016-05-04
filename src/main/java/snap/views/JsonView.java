package snap.views;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import snap.WebApplication;
import snap.http.RequestContext;

public class JsonView<T> extends View
{

  private int mStatus = HttpServletResponse.SC_OK;

  public JsonView(T root)
  {
    if (root == null)
      throw new IllegalArgumentException("Root object for JSON can't be null");
    mRoot = root;
    mapper = WebApplication.getInstance().getJsonMapper();
  }

  public JsonView(T root, int status)
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

    mapper.writeValue(os, mRoot);
  }

  private T mRoot;
  private ObjectMapper mapper;
}
