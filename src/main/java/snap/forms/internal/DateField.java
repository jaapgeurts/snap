package snap.forms.internal;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import snap.Helpers;
import snap.SnapException;
import snap.WebApplication;
import snap.forms.Form;

public class DateField extends FormFieldBase
{

  private static final String DEFAULT_DATE_PATTERN = "MM/dd/yyyy";

  public DateField(Form form, Field field, snap.forms.annotations.DateField annotation, String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class) && !field.getType().equals(Date.class)
        && !field.getType().equals(LocalDate.class) && !field.getType().equals(LocalDateTime.class)
        && !field.getType().equals(ZonedDateTime.class) && !field.getType().equals(Calendar.class))
      throw new IllegalArgumentException(
          "TextFields must be of type String, java.util.Date, LocalDate, LocalDateTime, ZonedDateTime or Calendar");

    mLabel = mAnnotation.label();
    if (!mAnnotation.placeholder().isEmpty())
      addAttribute("placeholder", mAnnotation.placeholder());

    if (!mAnnotation.id().isEmpty())
      mHtmlId = mAnnotation.id();

    setPattern(mAnnotation.pattern());

  }

  @Override
  public String render()
  {
    return render(getAttributes());
  }

  @Override
  public String render(Map<String, String> attributes)
  {
    if (!isVisible())
      return "";
    Object fieldValue = getFieldValue();

    String value;
    DateTimeFormatter dtFormatter;
    SimpleDateFormat sdFormat;
    // get the date format of the current request.
    Locale locale = WebApplication.getInstance().getRequestContext().getLocale();
    if (locale != null)
    {
      if (getPattern().isEmpty())
      {
        FormatStyle style = mAnnotation.formatStyle();
        dtFormatter = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
        int java7Style = convertJava8StyleToJava7Style(style);
        sdFormat = (SimpleDateFormat)DateFormat.getDateInstance(java7Style, locale);
      }
      else
      {
        dtFormatter = DateTimeFormatter.ofPattern(getPattern(), locale);
        sdFormat = new SimpleDateFormat(getPattern(), locale);
      }
    }
    else
    {
      if (getPattern().isEmpty())
        setPattern(DEFAULT_DATE_PATTERN);
      dtFormatter = DateTimeFormatter.ofPattern(getPattern());
      sdFormat = new SimpleDateFormat(getPattern());
    }
    // check the type
    // TODO: add the application or user selected locale here.
    if (fieldValue == null)
      value = "";
    else if (fieldValue instanceof String)
      value = Helpers.escapeHtml((String)fieldValue);
    else if (fieldValue instanceof java.util.Date)
      value = sdFormat.format(fieldValue);
    else if (fieldValue instanceof Calendar)
      value = sdFormat.format(((Calendar)fieldValue).getTime());
    else if (fieldValue instanceof LocalDate)
      value = ((LocalDate)fieldValue).format(dtFormatter);
    else if (fieldValue instanceof LocalDateTime)
      value = ((LocalDateTime)fieldValue).format(dtFormatter);
    else if (fieldValue instanceof ZonedDateTime)
      value = ((ZonedDateTime)fieldValue).format(dtFormatter);
    else
      throw new SnapException(
          "Only field types of String, java.util.Date, LocalDate, LocalDateTime, ZonedDateTime or Calendar are supported");

    return String.format("<input type='text' id='%1$s' name='%2$s' value='%3$s' %4$s/>\n", mHtmlId,
                         mFieldName, value, Helpers.attrToString(attributes));

  }

  @Override
  public void setFieldValue(String[] values)
  {
    try
    {
      if (values == null)
      {
        // There were no values submitted so just return
        return;
      }

      if (values.length > 1)
      {
        log.warn("Possible hacking attempt! Expected no more than one value for field '" + mField.getName()
            + "' but found: " + values.length);
      }

      DateTimeFormatter dtFormatter;
      SimpleDateFormat sdFormat;
      // get the date format of the current request.
      Locale locale = WebApplication.getInstance().getRequestContext().getLocale();
      if (locale != null)
      {
        if (getPattern().isEmpty())
        {
          FormatStyle style = mAnnotation.formatStyle();
          dtFormatter = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
          int java7Style = convertJava8StyleToJava7Style(style);
          sdFormat = (SimpleDateFormat)DateFormat.getDateInstance(java7Style, locale);
        }
        else
        {
          dtFormatter = DateTimeFormatter.ofPattern(getPattern(), locale);
          sdFormat = new SimpleDateFormat(getPattern(), locale);
        }
      }
      else
      {
        if (getPattern().isEmpty())
          setPattern(DEFAULT_DATE_PATTERN);
        dtFormatter = DateTimeFormatter.ofPattern(getPattern());
        sdFormat = new SimpleDateFormat(getPattern());
      }

      if (mField.getType().equals(String.class))
        super.setFieldValue(values);
      else if (mField.getType().equals(java.util.Date.class))
        mField.set(getFieldOwner(), sdFormat.parse(values[0]));
      else if (mField.getType().equals(Calendar.class))
        mField.set(getFieldOwner(), sdFormat.parse(values[0]));
      else if (mField.getType().equals(LocalDate.class))
        mField.set(getFieldOwner(), LocalDate.parse(values[0], dtFormatter));
      else if (mField.getType().equals(LocalDateTime.class))
        mField.set(getFieldOwner(), LocalDateTime.parse(values[0], dtFormatter));
      else if (mField.getType().equals(ZonedDateTime.class))
        mField.set(getFieldOwner(), ZonedDateTime.parse(values[0], dtFormatter));
      else
        throw new SnapException(
            "Only field types of String, java.util.Date, LocalDate, LocalDateTime, ZonedDateTime or Calendar are supported");
    }
    catch (ParseException | DateTimeParseException e)
    {
      mForm.onFieldAssignmentError(mFieldName, values[0], e);
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      String message = "Can't access field: " + mFieldName;
      log.debug(message, e);
      throw new SnapException(message, e);
    }
  }

  @Override
  public String toString()
  {
    return "DateField { " + mFieldName + " }";
  }

  public String getPattern()
  {
    return mPattern;
  }

  public void setPattern(String mPattern)
  {
    this.mPattern = mPattern;
  }

  /**
   * Converts a new Java 8 api Date Format Enum style to the old Java7 Date
   * Format INT style
   *
   * @param style
   * @return
   */
  private int convertJava8StyleToJava7Style(FormatStyle style)
  {
    switch(style)
    {
      case FULL:
        return DateFormat.FULL;
      case LONG:
        return DateFormat.LONG;
      case MEDIUM:
        return DateFormat.MEDIUM;
      case SHORT:
        return DateFormat.SHORT;
      default:
        return DateFormat.DEFAULT;
    }
  }

  private snap.forms.annotations.DateField mAnnotation;
  private String mPattern;

}
