package snap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import snap.http.HttpMethod;

/**
 * Describes the Http Methods that are allowed for this request.
 * Only valid for methods called by the router.
 * @author Jaap Geurts
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RouteOptions
{
  /**
   * Specify the methods that are allows. Common ones are:
   * HttpMethod.GET and HttpMethod.POST
   * @return A list of methods
   */
  HttpMethod[] methods() default {};
}
