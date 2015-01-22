package snap.forms.internal;

import java.lang.reflect.Field;

import snap.forms.Form;

public class HiddenField extends FormBase
{

  public HiddenField(Form form, Field field,
      snap.forms.annotations.HiddenField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("HiddenFields must be of type String");
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    return String.format(
        "<input type=\"hidden\" id=\"%1$s\" name=\"%2$s\" value=\"%3$s\"/>",
        mAnnotation.id(), mField.getName(), getFieldValue());
  }

  private snap.forms.annotations.HiddenField mAnnotation;
}
