package snap.forms.internal;

import java.lang.reflect.Field;
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
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    return String
        .format(
            "<input type=\"submit\" id=\"%1$s\" name=\"%2$s\" value=\"%3$s\" %4$s/>",
            mAnnotation.id(), mField.getName(), mLabel, getHtmlAttributes());
  }

  @Override
  public String toString()
  {
    return "SubmitButton { " + mField.getName() + " }";
  }

  private snap.forms.annotations.SubmitField mAnnotation;
}
