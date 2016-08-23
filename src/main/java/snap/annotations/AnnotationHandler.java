package snap.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import snap.http.RequestContext;

public interface AnnotationHandler
{
  /**
   * This method is executed when the matching registered annotation is
   * processed.
   *
   * @param controller
   *          The current controller object
   * @param method
   *          The controller method that's being called
   * @param annotation
   *          The annotation that is being processed
   * @param context
   *          The current request context.
   */
  void execute(Object controller, Method method, Annotation annotation, RequestContext context);
}
