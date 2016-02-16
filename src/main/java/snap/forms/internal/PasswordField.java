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
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    // Ignore the value parameter:: never set passwords in HTML
    StringBuilder sbuilder = new StringBuilder();

    if (hasError())
    {
      sbuilder.append("<span class='field-error'>");
      sbuilder.append(getError());
      sbuilder.append("</span>");
    }

    if (!"".equals(mLabel))
      sbuilder.append(String.format("<label for='%1$s'>%2$s</label>\n",
          mAnnotation.id(), mLabel));

    sbuilder.append(String.format(
        "<input type='password' id='%1$s' name='%2$s' %3$s/>\n",
        mAnnotation.id(), mField.getName(), getHtmlAttributes()));

    return sbuilder.toString();
  }

  @Override
  public String toString()
  {
    return "PasswordField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.PasswordField mAnnotation;
}
