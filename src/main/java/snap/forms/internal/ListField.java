package snap.forms.internal;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;
import snap.forms.ListOption;
import snap.forms.annotations.ListField.ListType;

public class ListField extends FormFieldBase
{

  public ListField(Form form, Field field,
      snap.forms.annotations.ListField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (mAnnotation.type() == ListType.MULTI_LIST)
    {
      if (!Set.class.isAssignableFrom(field.getType()))
        throw new IllegalArgumentException(
            "MultiList ListField must be of type Set<?>");
    }
    else
    {
      if (!(field.getType().equals(String.class)
          || field.getType().equals(Integer.class)
          || field.getType().equals(Long.class) || field.getType().isEnum()))
        throw new IllegalArgumentException(
            "DropDown and Single ListField must be of type String, Integer, Long or Enum");
    }

    mLabel = mAnnotation.label();
    addAttribute("class", mAnnotation.cssClass());
    mHtmlId = mAnnotation.id();
  }

  @Override
  public void setFieldValue(String[] values)
  {
    // Dropdown just returns a single value. set the String value;
    // TODO: also cast to Integer and Long
    if (mAnnotation.type() == ListType.SINGLE_LIST
        || mAnnotation.type() == ListType.DROPDOWN_LIST)
    {
      try
      {
        if (mField.getType().equals(String.class))
          super.setFieldValue(values);
        else if (mField.getType().equals(Integer.class))
          mField.set(mForm, Integer.valueOf(values[0]));
        else if (mField.getType().equals(Long.class))
          mField.set(mForm, Long.valueOf(values[0]));
        else if (mField.getType().isEnum())
          mField.set(mForm,
              Enum.valueOf((Class<Enum>)mField.getType(), values[0]));
        else
          throw new SnapException(
              "Only field types of String, Long, Integer and Enums are supported");
      }
      catch (NumberFormatException nfe)
      {
        log.warn("Possible hacking attempt! Submitted field value '" + values[0]
            + "' can't be converted to numeric type.", nfe);
      }
      catch (IllegalArgumentException | IllegalAccessException e)
      {
        String message = "Can't access field: " + mField.getName();
        log.debug(message, e);
        throw new SnapException(message, e);
      }
      return;
    }
    // TODO: still need to handle a multi-list

    getFormFields();
    mFieldValues.clear();

    if (values == null)
      return;

    if (mOptions == null)
      throw new SnapException("Did you forget to set the Options variable");

    for (String value : values)
    {
      if (mOptionFieldClass.isAssignableFrom(ListOption.class))
      {
        if (mOptions.stream()
            .anyMatch(obj -> ((ListOption)obj).getValue().equals(value)))
          addValueToFormFieldSet(value);
        else
          log.warn("Possible hacking attempt! Submitted field value '" + value
              + "' not found in options");
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
          log.warn("Possible hacking attempt! Submitted field value '" + value
              + "' not found in options");
      }
    }
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

    StringBuilder b = new StringBuilder();

    switch(mAnnotation.type())
    {
      case MULTI_LIST:
        b.append(String.format(
            "\n<select id='%1$s' name='%2$s' size='%3$s' multiple %4$s>\n",
            mAnnotation.id(), mField.getName(), mAnnotation.size(),
            Helpers.attrToString(attributes)));
        break;
      case DROPDOWN_LIST:
        b.append(String.format("\n<select id='%1$s' name='%2$s' %3$s>\n",
            mAnnotation.id(), mField.getName(),
            Helpers.attrToString(attributes)));
        break;
      case SINGLE_LIST:
        b.append(
            String.format("\n<select id='%1$s' name='%2$s' size='%3$s' %4$s>\n",
                mAnnotation.id(), mField.getName(), mAnnotation.size(),
                Helpers.attrToString(attributes)));
    }

    // Check if the field is present
    Field optionsField;
    try
    {
      optionsField = mForm.getClass().getField(mAnnotation.options());
    }
    catch (NoSuchFieldException nsfe)
    {
      throw new SnapException(
          "Options field '" + mAnnotation.options() + "' not present in form",
          nsfe);
    }

    // Check the field type
    String wrongTypeMessage = "Option field '" + mAnnotation.options()
        + "' must be of type List<Object> or List<ListOption>";
    if (!optionsField.getType().isAssignableFrom(List.class))
      throw new SnapException(wrongTypeMessage);

    List<?> options;
    try
    {
      options = (List<?>)optionsField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      throw new SnapException("Can't access field: " + mAnnotation.options(),
          e);
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
            "\t<option selected value='%1$s'>%2$s</option>\n", val, text));
      else
        b.append(
            String.format("\t<option value='%1$s'>%2$s</option>\n", val, text));
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
        throw new SnapException(
            "Currently only Set<String>, Set<Long>, Set<Integer> are supported");
    }
    catch (NumberFormatException nfe)
    {
      log.warn("Possible hacking attempt! Submitted field value '" + value
          + "' can't be converted to numeric type", nfe);
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
      throw new SnapException(
          "Options field '" + mAnnotation.options() + "' not present in form",
          nsfe);
    }

    // Get the options and the values
    try
    {
      mOptions = (List<?>)mOptionsField.get(mForm);
      if (mField.getType().isAssignableFrom(Set.class))
        mFieldValues = (Set<Object>)mField.get(mForm);
      if (mFieldValues == null)
        throw new SnapException("Field " + mField.getName()
            + " is null. Did you forget to initialize it?");
      // Get the type of the Set container./
      ParameterizedType pType = (ParameterizedType)mField.getGenericType();
      mOptionFieldClass = (Class<?>)pType.getActualTypeArguments()[0];

    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      throw new SnapException("Can't access field: " + mAnnotation.options(),
          e);
    }
  }

  @Override
  public String toString()
  {
    return "ListField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.ListField mAnnotation;
  private Field mOptionsField;
  private List<?> mOptions;
  private Set<Object> mFieldValues;
  private Class<?> mOptionFieldClass;
}
