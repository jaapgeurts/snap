package snap.forms.internal;

import java.lang.reflect.Field;

import snap.forms.Form;

public class TextArea extends FormField
{

  public TextArea(Form form, Field field,
      snap.forms.annotations.TextArea annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("TextAreaFields must be of type String");
  }

  @Override
  public String render()
  {
    String label = "";
    String value = getFieldValue();

    if (!"".equals(mAnnotation.label()))
      label = String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label());
    return String.format(
        "%1$s<textarea id=\"%2$s\" name=\"%3$s\">%4$s</textarea>\n", label,
        mAnnotation.id(), mField.getName(), value);

  }

  private snap.forms.annotations.TextArea mAnnotation;
}
