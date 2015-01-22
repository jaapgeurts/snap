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
      throw new IllegalArgumentException(
          "TextAreaFields must be of type String");
  }

  @Override
  public String render()
  {
    String label = "";
    String value = getFieldValue();
    String cols = "";
    String rows = "";

    if (!"".equals(mAnnotation.label()))
      label = String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label());

    if (mAnnotation.cols() > 0)
      cols = " cols=\"" + mAnnotation.cols() + "\" ";
    if (mAnnotation.rows() > 0)
      rows = " rows=\"" + mAnnotation.rows() + "\" ";

    return String.format(
        "%1$s<textarea id=\"%2$s\" name=\"%3$s\"%4$s%5$s>%6$s</textarea>\n",
        label, mAnnotation.id(), mField.getName(), cols, rows, value);

  }

  private snap.forms.annotations.TextArea mAnnotation;
}
