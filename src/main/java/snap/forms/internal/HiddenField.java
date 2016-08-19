package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;

public class HiddenField extends FormFieldBase
{

  public HiddenField(Form form, Field field, snap.forms.annotations.HiddenField annotation, String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (!(field.getType().equals(String.class) || field.getType().equals(Boolean.class)
        || field.getType().equals(Integer.class) || field.getType().equals(Long.class)))
      throw new IllegalArgumentException("HiddenFields must be of type String, Boolean, Integer, Long");

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

    return String.format("<input type='hidden' id='%1$s' name='%2$s' value='%3$s' %4$s/>", mHtmlId,
        mFieldName, getFieldValueString(), Helpers.attrToString(attributes));
  }

  @Override
  public void setFieldValue(String[] values)
  {
    try
    {

      if (values.length > 1)
      {
        log.warn("Possible hacking attempt! Expected one value for field '" + mFieldName + "' but found: "
            + values.length);
      }

      if (mField.getType().equals(String.class))
        super.setFieldValue(values);
      else if (mField.getType().equals(Boolean.class))
      {
        String val = values[0].trim().toLowerCase();
        if ("".equals(val) || !("true".equals(val) || "false".equals(val)))
          throw new SnapException("Only 'true' or 'false' are valid for boolean hidden fields");

        mField.set(getFieldOwner(), Boolean.valueOf(values[0]));
      }
      else if (mField.getType().equals(Integer.class))
        mField.set(getFieldOwner(), Integer.valueOf(values[0]));
      else if (mField.getType().equals(Long.class))
        mField.set(getFieldOwner(), Long.valueOf(values[0]));
      else
        throw new SnapException("Only field types of String, Boolean, Long, Integer are supported");

    }
    catch (NumberFormatException nfe)
    {
      log.warn("Possible hacking attempt! Submitted field value '" + values[0]
          + "' can't be converted to numeric type.", nfe);
    }
    catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchFieldException e)
    {
      String message = "Can't access field: " + mFieldName;
      log.debug(message, e);
      throw new SnapException(message, e);
    }
  }

  @Override
  public String toString()
  {
    return "HiddenField { " + mFieldName + " }";
  }

  private snap.forms.annotations.HiddenField mAnnotation;
}
