package snap.forms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this field as a TextField. The field can be a String, Integer or Long.
 * If you decide to use a numeric type make sure to validate the field with
 * javascript before submission or an error will be thrown.
 *
 * @author Jaap Geurts
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TextField
{
  public String id() default "";

  public String placeholder() default "";

  public String label() default "";

}
