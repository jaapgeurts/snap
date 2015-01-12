package snap.forms.internal;

import java.lang.reflect.Field;

import javax.servlet.http.Part;

import snap.forms.Form;

public class FileField extends FormField
{

  public FileField(Form form, Field field,
      snap.forms.annotations.FileField annotation)
  {
    super(form, field);
    mAnnotation = annotation;
    if (!field.getType().equals(Part.class))
      throw new IllegalArgumentException("FileFields must be of type Part");
  }

  @Override
  public String render()
  {
    String label = "";

    if (!"".equals(mAnnotation.label()))
      label = String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label());
    return String.format(
        "%1$s\n<input type=\"file\" id=\"%2$s\" name=\"%3$s\"><br/>", label,
        mAnnotation.id(), mField.getName());
  }

  //
  public void setFieldValue(Part part)
  {
    if (part == null)
    {
      log.warn("Possible hacking attempt! The expected part was not available");
      return;
    }
    try
    {
      mField.set(mForm, part);
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Can't access field: " + mField.getName();
      log.debug(message, e);
      throw new RuntimeException(message, e);
    }
  }

  private snap.forms.annotations.FileField mAnnotation;

}
