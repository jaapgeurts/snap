package snap.forms.internal;

import java.lang.reflect.Field;

import snap.forms.Form;

public class PasswordField extends FormField
{

  public PasswordField(Form form, Field field,
      snap.forms.annotations.PasswordField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("PasswordFields must be of type String");
  }

  @Override
  public String render()
  {
    // Ignore the value parameter:: never set passwords in HTML
    String label = "";

    if (!"".equals(mAnnotation.label()))
      label = String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label());
    return String.format(
        "%1$s<input type=\"password\" id=\"%2$s\" name=\"%3$s\">\n", label,
        mAnnotation.id(), mField.getName());
  }

  private snap.forms.annotations.PasswordField mAnnotation;
}
