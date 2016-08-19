package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;

public class TextField extends FormFieldBase
{

  public TextField(Form form, Field field, snap.forms.annotations.TextField annotation, String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class) && !field.getType().equals(Integer.class)
        && !field.getType().equals(Long.class))
      throw new IllegalArgumentException("TextFields must be of type String, Integer or Long");

    mLabel = mAnnotation.label();
    addAttribute("placeholder", mAnnotation.placeholder());

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
    String value = getFieldValueString();

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
      else if (mField.getType().equals(Integer.class))
        mField.set(getFieldOwner(), Integer.valueOf(values[0]));
      else if (mField.getType().equals(Long.class))
        mField.set(getFieldOwner(), Long.valueOf(values[0]));
      else
        throw new SnapException("Only field types of String, Long and Integer are supported");
    }
    catch (NumberFormatException nfe)
    {
      log.warn("Possible hacking attempt! Submitted field value '" + values[0]
          + "' can't be converted to numeric value.", nfe);
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
    return "TextField { " + mFieldName + " }";
  }

  private snap.forms.annotations.TextField mAnnotation;

}
