package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.SnapException;
import snap.forms.Form;

public class CheckBoxField extends FormFieldBase
{

  public CheckBoxField(Form form, Field field,
      snap.forms.annotations.CheckBoxField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(Boolean.class)
        && !field.getType().equals(boolean.class))
      throw new IllegalArgumentException(
          "CheckBoxFields must be of type Boolean or boolean");

    mLabel = mAnnotation.label();
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
          mAnnotation.id(), mField.getName(), attributesToString(attributes));
    else
      return String.format(
          "<input type='checkbox' id='%1$s' name='%2$s' value='%2$s' %3$s/>\n",
          mAnnotation.id(), mField.getName(), attributesToString(attributes));

  }

  @Override
  public void setFieldValue(String[] values)
  {

    try
    {
      if (values == null)
      {
        mField.set(mForm, Boolean.FALSE);
      }
      else
      {
        if (values.length > 1)
        {
          log.warn("Possible hacking attempt! Expected one value for field '"
              + mField.getName() + "' but found: " + values.length);
        }
        if (values[0].equals(mField.getName()))
        {
          mField.set(mForm, Boolean.TRUE);
        }
        else
        {
          log.warn("Possible hacking attempt! Expected value '"
              + mField.getName() + "' got value: '" + values[0]
              + "' for Field: " + mField.getName());
          mField.set(mForm, Boolean.FALSE);
        }
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
