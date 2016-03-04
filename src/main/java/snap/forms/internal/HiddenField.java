package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.forms.Form;

public class HiddenField extends FormFieldBase
{

  public HiddenField(Form form, Field field,
      snap.forms.annotations.HiddenField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("HiddenFields must be of type String");

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
        "<input type='hidden' id='%1$s' name='%2$s' value='%3$s' %4$s/>",
        mAnnotation.id(), mField.getName(), getFieldValue(),
        attributesToString(attributes));
  }

  @Override
  public String toString()
  {
    return "HiddenField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.HiddenField mAnnotation;
}
