package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
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

    // Ignore the value parameter:: never set passwords in HTML

    return String.format(
        "<input type='password' id='%1$s' name='%2$s' %3$s/>\n",
        mAnnotation.id(), mField.getName(), Helpers.attrToString(attributes));
  }

  @Override
  public String toString()
  {
    return "PasswordField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.PasswordField mAnnotation;
}
