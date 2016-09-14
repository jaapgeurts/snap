package snap.forms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.format.FormatStyle;

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
  String id() default "";

  String label() default "";

  String placeholder() default "";

  /**
   * Used when no Locale(language) is set for the current RequestContext.
   * Defaults to "MM/dd/yyyy"
   *
   * @return The pattern
   */
  String pattern() default "MM/dd/yyyy";

  /**
   * Used when a Locale(language) is set for the current RequestContext.
   * defaults to FormatStyle.MEDIUM
   *
   * @return the format style
   */
  FormatStyle formatStyle() default FormatStyle.MEDIUM;
}
