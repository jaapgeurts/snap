package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;

import snap.forms.Form;
import snap.forms.ListOption;
import snap.forms.annotations.MultiSelectField.MultiSelectType;

public class MultiSelectField extends FormField
{

  public MultiSelectField(Form form, Field field,
      snap.forms.annotations.MultiSelectField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(Set.class))
      throw new IllegalArgumentException("MultiSelectFields must be of type Set<String> or Set<ListOption>");
  }

  @Override
  public String render()
  {
    StringBuilder b = new StringBuilder();

    if (mAnnotation.type() == MultiSelectType.LIST)
      throw new NotImplementedException(
          "MultiSelectField LIST type is not yet implemented");

    // Checkbox
    // Check if the field is present
    getFormFields();

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
      // check type here.
      if (mFieldValues.contains(val))
        b.append(String
            .format(
                "\t<input type=\"checkbox\" name=\"%1$s\" value=\"%2$s\" checked>%3$s",
                mField.getName(), val, text));
      else
        b.append(String.format(
            "\t<input type=\"checkbox\" name=\"%1$s\" value=\"%2$s\">%3$s",
            mField.getName(), val, text));
    }
    return b.toString();
  }

  @Override
  public void setFieldValue(String[] values)
  {

    getFormFields();
    mFieldValues.clear();

    Object o = mOptions.stream().findFirst();
    boolean isListOption = false;
    if (o instanceof ListOption)
      isListOption = true;
    for (String value : values)
    {
      if (isListOption)
      {
        if (mOptions.stream().anyMatch(obj -> {
          return ((ListOption)obj).getValue().equals(value);
        }))
          mFieldValues.add(value);
        else
          log.warn("Possible hacking attempt! Submitted field value \"" + value
              + "\" not found in options");
      }
      else
      {
        if (mOptions.stream().anyMatch(obj -> {
          return obj.toString().equals(value);
        }))
          mFieldValues.add(value);
        else
          log.warn("Possible hacking attempt! Submitted field value \"" + value
              + "\" not found in options");
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
      mOptions = (Set<Object>)mOptionsField.get(mForm);
      if (mField.getType().isAssignableFrom(Set.class))
        mFieldValues = (Set<String>)mField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      throw new RuntimeException(
          "Can't access field: " + mAnnotation.options(), e);
    }
  }

  private snap.forms.annotations.MultiSelectField mAnnotation;

  private Field mOptionsField;
  private Set<Object> mOptions;
  private Set<String> mFieldValues;
}
