package snap.forms.internal;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

import snap.forms.Form;
import snap.forms.ListOption;
import snap.forms.annotations.ListField.ListType;

public class ListField extends FormBase
{

  public ListField(Form form, Field field,
      snap.forms.annotations.ListField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (mAnnotation.type() == ListType.MULTI_LIST)
    {
      if (!field.getType().equals(Set.class))
        throw new IllegalArgumentException(
            "MultiList ListField must be of type Set<?>");
    }
    else
    {
      if (!field.getType().equals(String.class))
        throw new IllegalArgumentException(
            "DropDown and Single ListField must be of type String");
    }

    mLabel = mAnnotation.label();
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public void setFieldValue(String[] values)
  {
    // Dropdown just returns a single value. set the String value;
    // TODO: also cast to Integer and Long
    if (mAnnotation.type() == ListType.SINGLE_LIST
        || mAnnotation.type() == ListType.DROPDOWN_LIST)
    {
      super.setFieldValue(values);
      return;
    }

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
        if (mOptions.stream().anyMatch(obj -> {
          if (obj instanceof ListOption)
          {
            return ((ListOption)obj).getValue().equals(value);
          }
          else
          {
            return obj.toString().equals(value);
          }
        }))
          addValueToFormFieldSet(value);
        else
          log.warn("Possible hacking attempt! Submitted field value \"" + value
              + "\" not found in options");
      }
    }
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    StringBuilder b = new StringBuilder();

    if (!"".equals(mAnnotation.label()))
      b.append(String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label()));

    switch(mAnnotation.type())
    {
      case MULTI_LIST:
        b.append(String
            .format(
                "\n<select id=\"%1$s\" name=\"%2$s\" class=\"%3$s\" size=\"%4$s\" multiple>\n",
                mAnnotation.id(), mField.getName(), mAnnotation.cssClass(),
                mAnnotation.size()));
        break;
      case DROPDOWN_LIST:
        b.append(String.format(
            "\n<select id=\"%1$s\" name=\"%2$s\" class=\"%3$s\" >\n",
            mAnnotation.id(), mField.getName(), mAnnotation.cssClass()));
        break;
      case SINGLE_LIST:
        b.append(String
            .format(
                "\n<select id=\"%1$s\" name=\"%2$s\" class=\"%3$s\" size=\"%4$s\">\n",
                mAnnotation.id(), mField.getName(), mAnnotation.cssClass(),
                mAnnotation.size()));
    }

    // Check if the field is present
    Field optionsField;
    try
    {
      optionsField = mForm.getClass().getField(mAnnotation.options());
    }
    catch (NoSuchFieldException nsfe)
    {
      throw new RuntimeException("Options field \"" + mAnnotation.options()
          + "\" not present in form", nsfe);
    }

    // Check the field type
    String wrongTypeMessage = "Option field \"" + mAnnotation.options()
        + "\" must be of type List<Object> or List<ListOption>";
    if (!optionsField.getType().isAssignableFrom(List.class))
      throw new RuntimeException(wrongTypeMessage);

    List<?> options;
    try
    {
      options = (List<?>)optionsField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      throw new RuntimeException(
          "Can't access field: " + mAnnotation.options(), e);
    }

    for (Object o : options)
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
      if (val.equals(getFieldValue()))
        b.append(String.format(
            "\t<option selected value=\"%1$s\">%2$s</option>\n", val, text));
      else
        b.append(String.format("\t<option value=\"%1$s\">%2$s</option>\n", val,
            text));
    }
    b.append("</select>");
    return b.toString();
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
    return "ListField [" + mField.getName() + "]";
  }

  private snap.forms.annotations.ListField mAnnotation;
  private Field mOptionsField;
  private List<?> mOptions;
  private Set<Object> mFieldValues;
  private Class<?> mOptionFieldClass;
}
