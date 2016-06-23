package snap.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import snap.http.RequestContext;

public interface AnnotationHandler
{
  public void execute(Class<?> controllerClass, Method method, Annotation annotation, RequestContext context);
}
