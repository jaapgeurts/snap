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
   * If set this is used regardless of whether the Locale on the RequestContext
   * is set. If set then formatStyle is ignored. If Locale is not set and this
   * is not specified than the pattern defaults to "MM/dd/yyyy"
   *
   * @return The pattern
   */
  String pattern() default "";

  /**
   * Used when a Locale(language) is set for the current RequestContext and
   * pattern is not set defaults to FormatStyle.MEDIUM
   *
   * @return the format style
   */
  FormatStyle formatStyle() default FormatStyle.MEDIUM;
}
