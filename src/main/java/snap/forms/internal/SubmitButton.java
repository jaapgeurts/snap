package snap.forms.internal;

import java.lang.reflect.Field;

import snap.forms.Form;

public class SubmitButton extends FormField
{

  public SubmitButton(Form form, Field field,
      snap.forms.annotations.SubmitField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("SubmitFields must be of type String");
  }

  @Override
  public String render()
  {
    return String
        .format(
            "<input type=\"submit\" id=\"%1$s\" name=\"%2$s\" value=\"%3$s\"><br/>",
            mAnnotation.id(), mField.getName(), mAnnotation.label());
  }

  private snap.forms.annotations.SubmitField mAnnotation;
}
