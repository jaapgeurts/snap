package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.forms.Form;

public class PasswordField extends FormFieldBase
{

  public PasswordField(Form form, Field field, snap.forms.annotations.PasswordField annotation,
      String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("PasswordFields must be of type String");

    mLabel = mAnnotation.label();
    if(!mAnnotation.placeholder().isEmpty())
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

    // Ignore the value parameter:: never set passwords in HTML

    return String.format("<input type='password' id='%1$s' name='%2$s' %3$s/>\n", mHtmlId,
        mFieldName, Helpers.attrToString(attributes));
  }

  @Override
  public String toString()
  {
    return "PasswordField { " + mFieldName + " }";
  }

  private snap.forms.annotations.PasswordField mAnnotation;
}
