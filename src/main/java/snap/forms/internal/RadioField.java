package snap.forms.internal;

import java.lang.reflect.Field;

import snap.forms.Form;

public class RadioField extends FormFieldBase
{

  public RadioField(Form form, Field field,
      snap.forms.annotations.RadioField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().isEnum())
      throw new IllegalArgumentException("RadioFieldsmust be an enum");
    
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    StringBuilder b = new StringBuilder();
    Object[] enumValues;

    Class<?> enumVal;
    enumVal = mField.getType();
    enumValues = enumVal.getEnumConstants();
    if (enumValues == null)
      throw new RuntimeException("RadioField works on Enums only!");

    Object value;
    try
    {
      value = mField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Form field " + mField.getName() + " can't be accessed.";
      log.debug(message, e);
      throw new RuntimeException(message, e);
    }
    for (Object e : enumValues)
    {
      // TODO:if type is Enum then create enum radio field else create
      // normal radio field

      if (e.equals(value))
        b.append(String
            .format(
                "<input id=\"%3$s\" type=\"radio\" name=\"%1$s\" value=\"%2$s\" checked/>%2$s",
                mField.getName(), e.toString(), mAnnotation.id()));
      else
        b.append(String
            .format(
                "<input id=\"%3$s\" type=\"radio\" name=\"%1$s\" value=\"%2$s\"/>%2$s",
                mField.getName(), e.toString(), mAnnotation.id()));

    }
    return b.toString();
  }

  @Override
  public void setFieldValue(String[] values)
  {
    // TODO: handle other types as well.
    // currently only enums supported
    // handle enums and set the correct value
    // Object[] enums = classField.getType().getEnumConstants();
    if (values == null)
    {
      log.warn("Possible hacking attempt! Expected return value for Radio button field \""
          + mField.getName() + "\" but found nothing");
    }
    else
    {
      if (values.length > 1)
      {
        log.warn("Possible hacking attempt! Expected one value for field \""
            + mField.getName() + "\" but found: " + values.length);
      }
      try
      {
        mField.set(mForm,
            Enum.valueOf((Class<Enum>)mField.getType(), values[0]));
      }
      catch (IllegalArgumentException iae)
      {
        log.warn("Possible hacking attempt! Expected legal value for enum: "
            + mField.getType().getName() + " but found: " + values[0]);
      }
      catch (IllegalAccessException e)
      {
        String message = "Can't access field: " + mField.getName();
        log.debug(message, e);
        throw new RuntimeException(message, e);
      }
    }
  }
  
  @Override
  public String toString()
  {
    return "RadioField { " + mField.getName() + " }";
  }


  private snap.forms.annotations.RadioField mAnnotation;

}
