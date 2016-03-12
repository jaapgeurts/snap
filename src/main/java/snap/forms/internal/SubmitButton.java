package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.forms.Form;

public class SubmitButton extends FormFieldBase
{

  public SubmitButton(Form form, Field field,
      snap.forms.annotations.SubmitField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("SubmitFields must be of type String");

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

    return String.format(
        "<input type='submit' id='%1$s' name='%2$s' value='%3$s' %4$s/>",
        mAnnotation.id(), mField.getName(), mLabel,
        Helpers.attrToString(attributes));
  }

  @Override
  public String toString()
  {
    return "SubmitButton { " + mField.getName() + " }";
  }

  private snap.forms.annotations.SubmitField mAnnotation;
}
