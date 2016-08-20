package snap.forms.internal;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;

public class DateField extends FormFieldBase
{

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
    addAttribute("placeholder", mAnnotation.placeholder());

    if (!mAnnotation.id().isEmpty())
      mHtmlId = mAnnotation.id();

    mDateFormatter = new SimpleDateFormat(mAnnotation.format());
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
    // check the type
    // TODO: add the application or user selected locale here.
    if (fieldValue == null)
      value = "";
    else if (fieldValue instanceof String)
      value = Helpers.escapeHtml((String)fieldValue);
    else if (fieldValue instanceof java.util.Date)
      value = mDateFormatter.format(fieldValue);
    else if (fieldValue instanceof Calendar)
      value = mDateFormatter.format(((Calendar)fieldValue).getTime());
    else if (fieldValue instanceof LocalDate)
      value = ((LocalDate)fieldValue).format(DateTimeFormatter.ofPattern(mAnnotation.format()));
    else if (fieldValue instanceof LocalDateTime)
      value = ((LocalDateTime)fieldValue).format(DateTimeFormatter.ofPattern(mAnnotation.format()));
    else if (fieldValue instanceof ZonedDateTime)
      value = ((ZonedDateTime)fieldValue).format(DateTimeFormatter.ofPattern(mAnnotation.format()));
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

      if (mField.getType().equals(String.class))
        super.setFieldValue(values);
      else if (mField.getType().equals(java.util.Date.class))
        mField.set(getFieldOwner(), mDateFormatter.parse(values[0]));
      else if (mField.getType().equals(Calendar.class))
        mField.set(getFieldOwner(), mDateFormatter.parse(values[0]));
      else if (mField.getType().equals(LocalDate.class))
        mField.set(getFieldOwner(),
            LocalDate.parse(values[0], DateTimeFormatter.ofPattern(mAnnotation.format())));
      else if (mField.getType().equals(LocalDateTime.class))
        mField.set(getFieldOwner(),
            LocalDateTime.parse(values[0], DateTimeFormatter.ofPattern(mAnnotation.format())));
      else if (mField.getType().equals(ZonedDateTime.class))
        mField.set(getFieldOwner(),
            ZonedDateTime.parse(values[0], DateTimeFormatter.ofPattern(mAnnotation.format())));
      else
        throw new SnapException(
            "Only field types of String, java.util.Date, LocalDate, LocalDateTime, ZonedDateTime or Calendar are supported");
    }
    catch (ParseException | DateTimeParseException e)
    {
      log.warn("Submitted field value '" + values[0] + "' can't be converted to date time value.", e);
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

  private snap.forms.annotations.DateField mAnnotation;
  private SimpleDateFormat mDateFormatter;

}
