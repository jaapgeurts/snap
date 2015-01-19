package snap.http;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import snap.Settings;
import snap.WebApplication;
import snap.views.TemplateView;
import snap.views.View;

public class HttpError implements RequestResult
{
  private static final String ERROR_PAGE_NAME = "snap-error.html";
  public HttpError(int errorCode)
  {
    mErrorCode = errorCode;
  }

  public HttpError(int errorCode, Throwable t)
  {
    this(errorCode);
    mException = t;
  }

  public HttpError(int errorCode, String message, Throwable t)
  {
    this(errorCode,t);
    mMessage = message;
  }

  @Override
  public void handleResult(RequestContext context) throws IOException
  {
    // Check properties.

    // If debug = true, render the result here.
    if (Settings.debug)
    {
      TemplateView view = new TemplateView(ERROR_PAGE_NAME);
      view.addParameter("exception", mException);
      view.addParameter("statuscode", mErrorCode);
      view.addParameter("message", mMessage);

      if (view != null)
      {
        HttpServletResponse r = context.getResponse();
        r.setStatus(mErrorCode);
        r.setContentType("text/html; charset=UTF-8");
        r.setCharacterEncoding("UTF-8");
        view.render(context);
      }
    }
    else
    {
      // Else call the user to display their standard ERROR pages.
      WebApplication webApp = WebApplication.getInstance();
      webApp.handleError(context, mErrorCode, mException);
    }
  }

  private int mErrorCode;
  private String mMessage;
  private Throwable mException;

}
