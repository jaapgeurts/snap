package snap.forms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MultiSelectField
{
  public enum MultiSelectType {
    CHECKBOX, LIST
  };

  public MultiSelectType type() default MultiSelectType.CHECKBOX;

  public String id() default "";

  public String cssClass() default "";

  public String options() default "";

}
