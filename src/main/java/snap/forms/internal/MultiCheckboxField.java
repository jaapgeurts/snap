package snap.forms.internal;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;
import snap.forms.ListOption;

public class MultiCheckboxField extends FormFieldBase
{

  public MultiCheckboxField(Form form, Field field, snap.forms.annotations.MultiCheckboxField annotation,
                            String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (!Set.class.isAssignableFrom(field.getType()))
      throw new IllegalArgumentException(
          "MultiCheckboxFields must be of type Set<String>, Set<Long>, Set<Integer> or Set<ListOption>");

    if (!mAnnotation.id().isEmpty())
      mHtmlId = mAnnotation.id();

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

    getFormFields();

    String which = attributes.get("which");
    if (which != null)
    {
      attributes.remove("which");
      // render just one
      Optional<?> optional = mOptions.stream().filter(o -> isValue(o, which)).findFirst();
      if (optional.isPresent())
        return doRender(optional.get(), attributes);
      else
        throw new SnapException(
            String.format("Can't render field for value %1$s of field %2$s", which, mFieldName));
    }
    else
    {
      // render all
      return mOptions.stream().map(o -> doRender(o, attributes)).collect(Collectors.joining("\n"));
    }
  }

  /**
   * Returns all possible field values for this field
   *
   * @return a list of strings
   */
  @Override
  public String[] getOptions()
  {
    getFormFields();

    return mOptions.stream().map(o -> {
      return o instanceof ListOption ? ((ListOption)o).getValue() : o.toString();
    }).collect(Collectors.toList()).toArray(new String[] {});

  }

  @Override
  public void setFieldValue(String[] values)
  {

    getFormFields();
    mFieldValues.clear();

    if (values == null)
      return;

    if (mOptions == null)
      throw new SnapException("Did you forget to set the Options variable");

    for (String value : values)
    {
      addValueToFormFieldSet(value);
    }
  }

  @Override
  public String toString()
  {
    return "MultiCheckBoxField { " + mFieldName + " }";
  }

  private boolean isValue(Object o, String key)
  {

    String val;
    if (o instanceof ListOption)
    {
      ListOption lo = (ListOption)o;
      val = lo.getValue();
    }
    else
    {
      val = o.toString();
    }

    return val.equals(key);
  }

  private String doRender(Object o, Map<String, String> attributes)
  {
    String val, text = "", htmlid;
    if (o instanceof ListOption)
    {
      ListOption lo = (ListOption)o;
      val = lo.getValue();
      text = lo.getText();
      htmlid = text;
    }
    else
    {
      val = o.toString();
      htmlid = val;
    }

    // check type here.
    if (mFieldValues.contains(val))
      return String.format("\t<input id='%1$s-%4$s' type='checkbox' name='%2$s' value='%3$s' checked %5$s/>",
                           mHtmlId, mFieldName, Helpers.escapeHtml(val), htmlid,
                           Helpers.attrToString(attributes));
    else
      return String.format("\t<input id='%1$s-%4$s' type='checkbox' name='%2$s' value='%3$s' %5$s/>",
                           mHtmlId, mFieldName, Helpers.escapeHtml(val), htmlid,
                           Helpers.attrToString(attributes));
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
        throw new SnapException("Currently only Set<String>, Set<Long>, Set<Integer> are supported");
    }
    catch (NumberFormatException nfe)
    {
      mForm.onFieldAssignmentError(mFieldName, value, nfe);
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
      throw new SnapException("Options field '" + mAnnotation.options() + "' not present in form", nsfe);
    }

    // Get the options and the values
    try
    {
      mOptions = (List<?>)mOptionsField.get(mForm);
      if (mField.getType().isAssignableFrom(Set.class))
        mFieldValues = (Set<Object>)mField.get(getFieldOwner());
      if (mFieldValues == null)
        throw new SnapException("Field " + mFieldName + " is null. Did you forget to initialize it?");
      // Get the type of the Set container./
      ParameterizedType pType = (ParameterizedType)mField.getGenericType();
      mOptionFieldClass = (Class<?>)pType.getActualTypeArguments()[0];

    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      throw new SnapException("Can't access field: " + mAnnotation.options(), e);
    }
  }

  private snap.forms.annotations.MultiCheckboxField mAnnotation;

  private Field mOptionsField;
  private List<?> mOptions;
  private Set<Object> mFieldValues;
  private Class<?> mOptionFieldClass;
}
