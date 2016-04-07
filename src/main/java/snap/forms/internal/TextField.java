package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.forms.Form;

public class TextField extends FormFieldBase
{

  public TextField(Form form, Field field, snap.forms.annotations.TextField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("TextFields must be of type String");

    mLabel = mAnnotation.label();
    addAttribute("placeholder", mAnnotation.placeholder());
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
    String value = getFieldValue();

    return String.format("<input type='text' id='%1$s' name='%2$s' value='%3$s' %4$s/>\n", mAnnotation.id(),
        mField.getName(), value, Helpers.attrToString(attributes));

  }

  @Override
  public String toString()
  {
    return "TextField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.TextField mAnnotation;

}
