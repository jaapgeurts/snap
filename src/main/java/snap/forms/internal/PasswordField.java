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
      throw new IllegalArgumentException("PasswordFields must be of type String");
    
    mLabel = mAnnotation.label();
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    // Ignore the value parameter:: never set passwords in HTML
    String label = "";

    if (!"".equals(mAnnotation.label()))
      label = String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label());
    return String.format(
        "%1$s<input type=\"password\" id=\"%2$s\" name=\"%3$s\"/>\n", label,
        mAnnotation.id(), mField.getName());
  }
  
  @Override
  public String toString()
  {
    return "PasswordField { " + mField.getName() + " }";
  }


  private snap.forms.annotations.PasswordField mAnnotation;
}
