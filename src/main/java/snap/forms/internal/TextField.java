package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;

import snap.forms.Form;

public class TextField extends FormFieldBase
{

  public TextField(Form form, Field field,
      snap.forms.annotations.TextField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("TextFields must be of type String");

    mLabel = mAnnotation.label();
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";
    String label = "";
    String value = getFieldValue();

    if (!"".equals(mLabel))
      label = String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mLabel);

    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, String> entry : mAttributes.entrySet())
    {
      builder.append(entry.getKey());
      builder.append("=\"");
      builder.append(entry.getValue());
      builder.append("\" ");
    }
    return String
        .format(
            "%1$s\n<input type=\"text\" id=\"%2$s\" name=\"%3$s\" value=\"%4$s\" %5$s/>\n",
            label, mAnnotation.id(), mField.getName(), value,
            builder.toString());
  }

  @Override
  public String toString()
  {
    return "TextField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.TextField mAnnotation;

}
