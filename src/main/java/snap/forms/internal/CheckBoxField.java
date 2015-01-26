package snap.forms.internal;

import java.lang.reflect.Field;

import snap.forms.Form;

public class CheckBoxField extends FormBase
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
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    String labelPre = "";
    String labelPost = "";

    if (!"".equals(mAnnotation.label()))
    {
      labelPre = String.format("<label for=\"%1$s\">", mAnnotation.id());
      labelPost = String.format("%1$s</label>", mAnnotation.label());
    }
    if (!mField.getType().equals(Boolean.class)
        && !mField.getType().equals(boolean.class))
      throw new RuntimeException(
          "CheckBoxField works on boolean primitives or Boolean classes only!)");

    // values are alwayes auto boxed
    boolean val;
    try
    {
      val = (Boolean)mField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      log.debug("Can't access value of form field: " + mField.getName(), e);
      throw new RuntimeException("Form field " + mField.getName()
          + " can't be accessed.", e);
    }

    if (val)
      return String
          .format(
              "%1$s\n<input type=\"checkbox\" id=\"%2$s\" name=\"%3$s\" value=\"%3$s\" checked/>%4$s\n",
              labelPre, mAnnotation.id(), mField.getName(), labelPost);
    else
      return String
          .format(
              "%1$s\n<input type=\"checkbox\" id=\"%2$s\" name=\"%3$s\" value=\"%3$s\"/>%4$s\n",
              labelPre, mAnnotation.id(), mField.getName(), labelPost);

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
          log.warn("Possible hacking attempt! Expected one value for field \""
              + mField.getName() + "\" but found: " + values.length);
        }
        if (values[0].equals(mField.getName()))
        {
          mField.set(mForm, Boolean.TRUE);
        }
        else
        {
          log.warn("Possible hacking attempt! Expected value \""
              + mField.getName() + "\" got value: \"" + values[0]
              + "\" for Field: " + mField.getName());
          mField.set(mForm, Boolean.FALSE);
        }
      }
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Can't access field: " + mField.getName();
      log.debug(message, e);
      throw new RuntimeException(message, e);
    }
  }

  @Override
  public String toString()
  {
    return "CheckBoxField [" + mField.getName() + "]";
  }

  private snap.forms.annotations.CheckBoxField mAnnotation;
}
