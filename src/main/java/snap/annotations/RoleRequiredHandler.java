package snap.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import snap.AuthenticationException;
import snap.AuthorizationException;
import snap.User;
import snap.http.RequestContext;

public class RoleRequiredHandler implements AnnotationHandler
{

  @Override
  public void execute(Object controller, Method method, Annotation annotation, RequestContext context)
  {
    User user = context.getAuthenticatedUser();
    if (user == null)
      throw new AuthenticationException(
          "Not allowed to access URL: " + context.getRequest().getPathInfo() + ". User not Authenticated");

    String[] roles = ((RoleRequired)annotation).roles();
    boolean hasRole = Arrays.stream(roles).anyMatch(role -> user.hasRole(role));

    if (!hasRole)
      throw new AuthorizationException(
          "Not allowed to access URL: " + context.getRequest().getPathInfo() + ". User not Authorized");
  }

}
