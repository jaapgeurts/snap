package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.List;
import snap.forms.Form;
import snap.forms.ListOption;

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

    getFormFields();

    StringBuilder b = new StringBuilder();

    Class<?> enumVal;
    enumVal = mField.getType();
    if (!enumVal.isEnum())
      throw new RuntimeException("RadioField works on Enums only!");

    String defaultValue;
    try
    {
      defaultValue = ((Enum<?>)mField.get(mForm)).name();
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Form field " + mField.getName() + " can't be accessed.";
      log.debug(message, e);
      throw new RuntimeException(message, e);
    }
    for (Object o : mOptions)
    {
      String val, text;
      if (o instanceof ListOption)
      {
        ListOption lo = (ListOption)o;
        val = lo.getValue();
        text = lo.getText();
      }
      else
      {
        val = text = o.toString();
      }

      if (val.equals(defaultValue))
        b.append(String
            .format(
                "<input id=\"%1$s\" type=\"radio\" name=\"%2$s\" value=\"%3$s\" checked/><label for=\"%1$s\"> %4$s</label>",
                mAnnotation.id(), mField.getName(), val, text));
      else
        b.append(String
            .format(
                "<input id=\"%1$s\" type=\"radio\" name=\"%2$s\" value=\"%3$s\"/><label for=\"%1$s\"> %4$s</label>",
                mAnnotation.id(), mField.getName(), val, text));

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

  private void getFormFields()
  {

    try
    {
      mOptionsField = mForm.getClass().getField(mAnnotation.options());
    }
    catch (NoSuchFieldException nsfe)
    {
      throw new RuntimeException("Options field \"" + mAnnotation.options()
          + "\" not present in form", nsfe);
    }

    // Get the options and the values
    try
    {
      mOptions = (List<?>)mOptionsField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      throw new RuntimeException(
          "Can't access field: " + mAnnotation.options(), e);
    }
  }

  @Override
  public String toString()
  {
    return "RadioField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.RadioField mAnnotation;
  private List<?> mOptions;
  private Field mOptionsField;

}
