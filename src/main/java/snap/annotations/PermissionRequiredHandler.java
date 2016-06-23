package snap.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import snap.AuthenticationException;
import snap.AuthorizationException;
import snap.User;
import snap.http.RequestContext;

public class PermissionRequiredHandler implements AnnotationHandler
{

  @Override
  public void execute(Class<?> controllerClass, Method method, Annotation annotation, RequestContext context)
  {
    User user = context.getAuthenticatedUser();
    if (user == null)
      throw new AuthenticationException(
          "Not allowed to access URL: " + context.getRequest().getPathInfo() + ". User not Authenticated");

    String[] rights = ((PermissionRequired)annotation).permissions();
    boolean hasRight = Arrays.stream(rights).anyMatch(permission -> user.hasPermission(permission));
    if (!hasRight)
      throw new AuthorizationException(
          "Not allowed to access URL: " + context.getRequest().getPathInfo() + ". User not Authorized");
  }

}
