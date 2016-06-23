package snap.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.AuthenticationException;
import snap.WebApplication;
import snap.http.Authenticator;
import snap.http.RequestContext;

public class LoginRequiredHandler implements AnnotationHandler
{
  final Logger log = LoggerFactory.getLogger(LoginRequiredHandler.class);

  @Override
  public void execute(Object controller, Method method, Annotation annotation,
      RequestContext context)
  {
    
    if ((!controller.getClass().isAnnotationPresent(LoginRequired.class)
        || method.isAnnotationPresent(IgnoreLoginRequired.class))
        && !method.isAnnotationPresent(LoginRequired.class))
      return;

    if (context.getAuthenticatedUser() == null)
    {
      boolean isAuthenticated = false;
      String authHeader = context.getHeader("Authorization");
      if (authHeader != null)
      {
        boolean authMethodMatched = false;
        // attempt authenticate via a registered authenticator
        for (Authenticator authenticator : WebApplication.getInstance().getAuthenticators())
        {
          if (authenticator.matchAuthenticationHeader(authHeader))
          {
            authMethodMatched = true;
            isAuthenticated = authenticator.authenticate(context, authHeader);
            if (isAuthenticated)
              break;
          }
        }

        if (!authMethodMatched)
          log.info("No suitable authenticator for authentication method: " + authHeader);
      }
      else
      {
        log.debug("User not authenticated and no Authorization header sent");
      }

      if (!isAuthenticated)
      {
        throw new AuthenticationException(
            "Not allowed to access URL: " + context.getRequest().getPathInfo() + ". User not Authenticated");
      }
    }
  }

}
