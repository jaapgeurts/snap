package snap.forms.internal;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

import snap.forms.Form;
import snap.forms.ListOption;

public class MultiCheckboxField extends FormBase
{

  public MultiCheckboxField(Form form, Field field,
      snap.forms.annotations.MultiCheckboxField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(Set.class))
      throw new IllegalArgumentException(
          "MultiCheckboxFields must be of type Set<String>, Set<Long>, Set<Integer> or Set<ListOption>");

    mCssClass = mAnnotation.cssClass();
  }

  /**
   * Renders a multiselect item identified by it's value. Only works for
   * checkboxes
   * 
   * @param value
   * @return
   */
  public String render(String value)
  {
    if (!isVisible())
      return "";

    StringBuilder b = new StringBuilder();

    // Checkbox
    // Check if the field is present
    getFormFields();

    // search all options
    for (Object o : mOptions)
    {
      String val, text = "";
      if (o instanceof ListOption)
      {
        ListOption lo = (ListOption)o;
        val = lo.getValue();
        text = lo.getText();
      }
      else
      {
        val = o.toString();
      }

      if (val.equals(value))
      {
        // check type here.
        if (mFieldValues.contains(val))
          b.append(String
              .format(
                  "\t<input type=\"checkbox\" name=\"%1$s\" value=\"%2$s\" checked/>%3$s",
                  mField.getName(), val, text));
        else
          b.append(String.format(
              "\t<input type=\"checkbox\" name=\"%1$s\" value=\"%2$s\"/>%3$s",
              mField.getName(), val, text));
      }
    }
    return b.toString();
  }

  @Override
  public String render()
  {
    StringBuilder b = new StringBuilder();

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
                "\t<input type=\"checkbox\" name=\"%1$s\" value=\"%2$s\" checked/>%3$s",
                mField.getName(), val, text));
      else
        b.append(String.format(
            "\t<input type=\"checkbox\" name=\"%1$s\" value=\"%2$s\"/>%3$s",
            mField.getName(), val, text));
    }
    return b.toString();
  }

  @Override
  public void setFieldValue(String[] values)
  {

    getFormFields();
    mFieldValues.clear();

    if (values == null)
      return;

    if (mOptions == null)
      throw new RuntimeException("Did you forget to set the Options variable");

    for (String value : values)
    {
      if (mOptionFieldClass.isAssignableFrom(ListOption.class))
      {
        if (mOptions.stream().anyMatch(
            obj -> ((ListOption)obj).getValue().equals(value)))
          addValueToFormFieldSet(value);
        else
          log.warn("Possible hacking attempt! Submitted field value \"" + value
              + "\" not found in options");
      }
      else
      {
        // content is another type of object
        if (mOptions.stream().anyMatch(obj -> obj.toString().equals(value)))
          addValueToFormFieldSet(value);
        else
          log.warn("Possible hacking attempt! Submitted field value \"" + value
              + "\" not found in options");
      }
    }
  }

  private void addValueToFormFieldSet(String value)
  {
    try
    {
      if (mOptionFieldClass.equals(String.class))
        mFieldValues.add(value);
      if (mOptionFieldClass.equals(Long.class))
        mFieldValues.add(Long.valueOf(value));
      else if (mOptionFieldClass.equals(Integer.class))
        mFieldValues.add(Integer.valueOf(value));
      else
        throw new RuntimeException(
            "Currently only Set<String>, Set<Long>, Set<Integer> are supported");
    }
    catch (NumberFormatException nfe)
    {
      log.warn("Possible hacking attempt! Submitted field value \"" + value
          + "\" can't be converted to numeric type");
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
      if (mField.getType().isAssignableFrom(Set.class))
        mFieldValues = (Set<Object>)mField.get(mForm);
      if (mFieldValues == null)
        throw new RuntimeException("Field " + mField.getName()
            + " is null. Did you forget to initialize it?");
      // Get the type of the Set container./
      ParameterizedType pType = (ParameterizedType)mField.getGenericType();
      mOptionFieldClass = (Class<?>)pType.getActualTypeArguments()[0];

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
    return "MultiCheckBoxField [" + mField.getName() + "]";
  }


  private snap.forms.annotations.MultiCheckboxField mAnnotation;

  private Field mOptionsField;
  private List<?> mOptions;
  private Set<Object> mFieldValues;
  private Class<?> mOptionFieldClass;
}
