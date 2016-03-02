package snap.forms.internal;

import java.lang.reflect.Field;
import snap.forms.Form;

public class PasswordField extends FormFieldBase
{

  public PasswordField(Form form, Field field,
      snap.forms.annotations.PasswordField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException(
          "PasswordFields must be of type String");

    mLabel = mAnnotation.label();
    mCssClass = mAnnotation.cssClass();
    mHtmlId = mAnnotation.id();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    // Ignore the value parameter:: never set passwords in HTML

    return String.format(
        "<input type='password' id='%1$s' name='%2$s' %3$s/>\n",
        mAnnotation.id(), mField.getName(), getHtmlAttributes());
  }

  @Override
  public String toString()
  {
    return "PasswordField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.PasswordField mAnnotation;
}
