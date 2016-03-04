package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.forms.Form;

public class TextArea extends FormFieldBase
{

  public TextArea(Form form, Field field,
      snap.forms.annotations.TextArea annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException(
          "TextAreaFields must be of type String");

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

    String value = getFieldValue();
    String cols = "";
    String rows = "";

    if (mAnnotation.cols() > 0)
      cols = " cols='" + mAnnotation.cols() + "' ";
    if (mAnnotation.rows() > 0)
      rows = " rows='" + mAnnotation.rows() + "' ";

    return String.format(
        "<textarea id='%1$s' name='%2$s'%3$s%4$s %6$s>%5$s</textarea>\n",
        mAnnotation.id(), mField.getName(), cols, rows, value, attributesToString(attributes));

  }

  @Override
  public String toString()
  {
    return "TextArea { " + mField.getName() + " }";
  }

  private snap.forms.annotations.TextArea mAnnotation;
}
