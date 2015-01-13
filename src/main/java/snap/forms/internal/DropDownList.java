package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.List;

import snap.forms.Form;
import snap.forms.ListOption;

public class DropDownList extends FormField
{

  public DropDownList(Form form, Field field,
      snap.forms.annotations.DropDownList annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(String.class))
      throw new IllegalArgumentException("FileFields must be of type String");
  }

  @Override
  public String render()
  {
    StringBuilder b = new StringBuilder();

    if (!"".equals(mAnnotation.label()))
      b.append(String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label()));
    b.append(String.format(
        "\n<select id=\"%1$s\" name=\"%2$s\"><br/>\n",
        mAnnotation.id(), mField.getName()));

    // Check if the field is present
    Field optionsField;
    try
    {
      optionsField = mForm.getClass().getField(mAnnotation.options());
    }
    catch (NoSuchFieldException nsfe)
    {
      throw new RuntimeException("Options field \"" + mAnnotation.options()
          + "\" not present in form", nsfe);
    }

    // Check the field type
    String wrongTypeMessage = "Option field \"" + mAnnotation.options()
        + "\" must be of type List<Object> or List<ListOption>";
    if (!optionsField.getType().isAssignableFrom(List.class))
      throw new RuntimeException(wrongTypeMessage);

    List<?> options;
    try
    {
      options = (List<?>)optionsField.get(mForm);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      throw new RuntimeException(
          "Can't access field: " + mAnnotation.options(), e);
    }
    
    for (Object o : options)
    {
      String val, text;
      if (o instanceof ListOption)
      {
        ListOption lo = (ListOption)o;
        val = lo.getValue();
        text = lo.getText();
      }
      else
      {
        val = text = o.toString();
      }
      if (val.equals(getFieldValue()))
        b.append(String.format(
            "\t<option selected value=\"%1$s\">%2$s</option>\n", val, text));
      else
        b.append(String.format("\t<option value=\"%1$s\">%2$s</option>\n", val,
            text));
    }
    b.append("</select>");
    return b.toString();
  }
  
  private snap.forms.annotations.DropDownList mAnnotation;
}
