package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;

public class CheckBoxField extends FormFieldBase
{

  public CheckBoxField(Form form, Field field, snap.forms.annotations.CheckBoxField annotation,
      String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (!field.getType().equals(Boolean.class))
      throw new IllegalArgumentException("CheckBoxFields must be of type Boolean");

    mLabel = mAnnotation.label();
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

    if (!mField.getType().equals(Boolean.class) && !mField.getType().equals(boolean.class))
      throw new SnapException("CheckBoxField works on boolean primitives or Boolean classes only!)");

    // values are always auto boxed
    boolean val;
    try
    {
      if (mField.get(getFieldOwner()) == null)
        throw new SnapException("Checkbox field: " + mFieldName + " can't be null.");
      val = (Boolean)mField.get(getFieldOwner());
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      log.debug("Can't access value of form field: " + mFieldName, e);
      throw new SnapException("Form field " + mFieldName + " can't be accessed.", e);
    }

    return String.format(
        "<input type='checkbox' id='%1$s' name='%2$s' value='true' %3$s%4$s/>\n<input type='hidden' value='false' name='%2$s'/>\n",
        mHtmlId, mFieldName, val ? "checked " : "", Helpers.attrToString(attributes));

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

      if (values.length > 2)
      {
        log.warn("Possible hacking attempt! Expected no more than two values for field '" + mFieldName
            + "' but found: " + values.length);
      }

      // Always take the first value. If there is two values it means the
      // checkbox checked and thus submitted
      // if there is only one value it means the checkbox was not submitted so
      // the first value now is the hidden value
      // according to this link:
      // http://stackoverflow.com/questions/4027635/do-browsers-preserve-order-of-inputs-with-same-name-on-get-post
      // the order is preserved
      mField.set(getFieldOwner(), Boolean.valueOf(values[0]));

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
    return "CheckBoxField { " + mFieldName + " }";
  }

  private snap.forms.annotations.CheckBoxField mAnnotation;
}
