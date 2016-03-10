package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import snap.SnapException;
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

    addAttribute("class", mAnnotation.cssClass());
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

    String defaultValue = getDefaultValue();

    String which = attributes.get("which");
    if (which != null)
    {
      removeAttribute("which");
      // render just one
      Optional<?> optional = mOptions.stream().filter(o -> isValue(o, which))
          .findFirst();
      if (!optional.isPresent())
        throw new SnapException(
            String.format("Can't render field for value %1$s of field %2$s",
                which, mField.getName()));
      return doRender(optional.get(), defaultValue, attributes);

    }
    else
    {
      // render all
      return mOptions.stream().map(o -> doRender(o, defaultValue, attributes))
          .collect(Collectors.joining("\n"));
    }
  }

  /**
   * Gets the HTML display label that should be used for this field. This method
   * is used only in fields that hold multiple select values such as radios or
   * lists
   * 
   * @param which
   * @return
   */
  @Override
  public String getLabel(String which)
  {
    getFormFields();

    Optional<?> optional = mOptions.stream().filter(o -> isValue(o, which))
        .findFirst();
    if (!optional.isPresent())
      throw new SnapException(String.format(
          "Can't get label for field for value %1$s of field %2$s", which,
          mField.getName()));

    String val;
    Object o = optional.get();
    if (o instanceof ListOption)
    {
      ListOption lo = (ListOption)o;
      val = lo.getText();
    }
    else
    {
      val = o.toString();
    }
    return val;
  }

  public String getHtmlId(String which)
  {
    getFormFields();

    Optional<?> optional = mOptions.stream().filter(o -> isValue(o, which))
        .findFirst();
    if (!optional.isPresent())
      throw new SnapException(String.format(
          "Can't get label for field for value %1$s of field %2$s", which,
          mField.getName()));

    String val;
    Object o = optional.get();
    if (o instanceof ListOption)
    {
      ListOption lo = (ListOption)o;
      val = lo.getValue();
    }
    else
    {
      val = o.toString();
    }
    return mHtmlId + '-' + val;
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
      log.warn(
          "Possible hacking attempt! Expected return value for Radio button field '"
              + mField.getName() + "' but found nothing");
    }
    else
    {
      if (values.length > 1)
      {
        log.warn("Possible hacking attempt! Expected one value for field '"
            + mField.getName() + "' but found: " + values.length);
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
        throw new SnapException(message, e);
      }
    }
  }

  @Override
  public String toString()
  {
    return "RadioField { " + mField.getName() + " }";
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

  private String doRender(Object o, String defaultValue,
      Map<String, String> attributes)
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

    if (val.equals(defaultValue))
      return String.format(
          "<input id='%1$s-%3$s' type='radio' name='%2$s' value='%3$s' checked $4$s/>",
          mAnnotation.id(), mField.getName(), val,
          attributesToString(attributes));
    else
      return String.format(
          "<input id='%1$s-%3$s' type='radio' name='%2$s' value='%3$s' $4$s/>",
          mAnnotation.id(), mField.getName(), val,
          attributesToString(attributes));

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
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      throw new SnapException("Can't access field: " + mAnnotation.options(),
          e);
    }
  }

  private String getDefaultValue()
  {
    String defaultValue = null;
    try
    {
      if (mField.get(mForm) == null)
        throw new SnapException("Enum formfield: " + mField.getName()
            + " can't be null and must be assigned a value.");
      defaultValue = ((Enum<?>)mField.get(mForm)).name();
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Form field " + mField.getName() + " can't be accessed.";
      log.debug(message, e);
      throw new SnapException(message, e);
    }
    return defaultValue;
  }

  private snap.forms.annotations.RadioField mAnnotation;
  private List<?> mOptions;
  private Field mOptionsField;

}
