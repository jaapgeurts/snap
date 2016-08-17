package snap.forms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this field as a CheckBox. The field type must be a String. When
 * generated it will create the checkbox and a hiddenfield with the same ID.
 *
 * @author Jaap Geurts
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CheckBoxField
{
  public String id() default "";

  public String label() default "";

}
