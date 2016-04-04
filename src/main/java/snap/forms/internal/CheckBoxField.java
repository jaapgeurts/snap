package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;

public class CheckBoxField extends FormFieldBase
{

  public CheckBoxField(Form form, Field field,
      snap.forms.annotations.CheckBoxField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(Boolean.class))
      throw new IllegalArgumentException(
          "CheckBoxFields must be of type Boolean");

    mLabel = mAnnotation.label();
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

    if (!mField.getType().equals(Boolean.class)
        && !mField.getType().equals(boolean.class))
      throw new SnapException(
          "CheckBoxField works on boolean primitives or Boolean classes only!)");

    // values are always auto boxed
    boolean val;
    try
    {
      if (mField.get(mForm) == null)
        throw new SnapException(
            "Checkbox field: " + mField.getName() + " can't be null.");
      val = (Boolean)mField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      log.debug("Can't access value of form field: " + mField.getName(), e);
      throw new SnapException(
          "Form field " + mField.getName() + " can't be accessed.", e);
    }

    if (val)
      return String.format(
          "<input type='checkbox' id='%1$s' name='%2$s' value='%2$s' checked %3$s/>\n",
          mAnnotation.id(), mField.getName(), Helpers.attrToString(attributes));
    else
      return String.format(
          "<input type='checkbox' id='%1$s' name='%2$s' value='%2$s' %3$s/>\n",
          mAnnotation.id(), mField.getName(), Helpers.attrToString(attributes));

  }

  @Override
  public void setFieldValue(String[] values)
  {

    try
    {
      if (values != null && values.length > 1)
      {
        log.warn("Possible hacking attempt! Expected one value for field '"
            + mField.getName() + "' but found: " + values.length);
      }

      // Checkboxes only post values when they are checked.
      // if they are not checked they are not posted back
      // so always set it to false first.
      mField.set(mForm, Boolean.FALSE);
      if (values == null)
      {
        // There were no values submitted to just return
        return;
      }
      if (values[0].equals(mField.getName()))
      {
        // And set it to true if we found the field.
        mField.set(mForm, Boolean.TRUE);
      }
      else
      {
        log.warn("Possible hacking attempt! Expected value '" + mField.getName()
            + "' got value: '" + values[0] + "' for Field: "
            + mField.getName());
      }

    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Can't access field: " + mField.getName();
      log.debug(message, e);
      throw new SnapException(message, e);
    }
  }

  @Override
  public String toString()
  {
    return "CheckBoxField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.CheckBoxField mAnnotation;
}
