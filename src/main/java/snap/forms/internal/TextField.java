package snap.forms.internal;

import java.lang.reflect.Field;
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
    String value = getFieldValue();

    StringBuilder sbuilder = new StringBuilder();

    if (!"".equals(mLabel))
      sbuilder.append(String.format("<label for='%1$s'>%2$s</label>\n",
          mAnnotation.id(), mLabel));

   
    sbuilder
        .append(String
            .format(
                "<input type='text' id='%1$s' name='%2$s' value='%3$s' %4$s/>\n",
                mAnnotation.id(), mField.getName(), value, getHtmlAttributes()));

    return sbuilder.toString();
  }

  @Override
  public String toString()
  {
    return "TextField { " + mField.getName() + " }";
  }

  private snap.forms.annotations.TextField mAnnotation;

}
