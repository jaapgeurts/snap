package snap.forms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this field as a DateInput. The field type must be a String,
 * java.util.Date, Calendar, LocalDate, LocalDateTime, ZonedDateTime. When the
 * html is generated for this field it will generate a standard text field. The
 * default date format is the US style 'MM/dd/yyyy'
 *
 * @author Jaap Geurts
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DateField
{
  public String id() default "";

  public String label() default "";

  public String placeholder() default "";

  public String format() default "MM/dd/yyyy";
}
