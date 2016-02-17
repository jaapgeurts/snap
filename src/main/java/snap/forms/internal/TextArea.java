package snap.forms.internal;

import java.lang.reflect.Field;
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
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    String value = getFieldValue();
    String cols = "";
    String rows = "";

    StringBuilder sbuilder = new StringBuilder();

    if (!"".equals(mAnnotation.label()))
      sbuilder.append(String.format("<label for='%1$s'>%2$s</label>\n",
          mAnnotation.id(), mAnnotation.label()));

    if (mAnnotation.cols() > 0)
      cols = " cols='" + mAnnotation.cols() + "' ";
    if (mAnnotation.rows() > 0)
      rows = " rows='" + mAnnotation.rows() + "' ";

    sbuilder.append(String.format(
        "<textarea id='%1$s' name='%2$s'%3$s%4$s %6$s>%5$s</textarea>\n",
        mAnnotation.id(), mField.getName(), cols, rows, value,
        getHtmlAttributes()));

    return sbuilder.toString();

  }

  @Override
  public String toString()
  {
    return "TextArea { " + mField.getName() + " }";
  }

  private snap.forms.annotations.TextArea mAnnotation;
}
