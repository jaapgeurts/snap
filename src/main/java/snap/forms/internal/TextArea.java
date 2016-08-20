package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.Helpers;
import snap.forms.Form;

public class TextArea extends FormFieldBase
{

  public TextArea(Form form, Field field, snap.forms.annotations.TextArea annotation, String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("TextAreaFields must be of type String");

    mLabel = mAnnotation.label();
    if (!mAnnotation.id().isEmpty())
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

    String value = getFieldValueString();
    String cols = "";
    String rows = "";

    if (mAnnotation.cols() > 0)
      cols = " cols='" + mAnnotation.cols() + "' ";
    if (mAnnotation.rows() > 0)
      rows = " rows='" + mAnnotation.rows() + "' ";

    return String.format("<textarea id='%1$s' name='%2$s'%3$s%4$s %6$s>%5$s</textarea>\n", mHtmlId,
        mFieldName, cols, rows, Helpers.escapeHtml(value), Helpers.attrToString(attributes));

  }

  @Override
  public String toString()
  {
    return "TextArea { " + mFieldName + " }";
  }

  private snap.forms.annotations.TextArea mAnnotation;
}
