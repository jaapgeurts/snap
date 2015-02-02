package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Set;

import javax.servlet.http.Part;

import snap.forms.Form;

public class FileField extends FormBase
{

  public FileField(Form form, Field field,
      snap.forms.annotations.FileField annotation)
  {
    // TODO: check if template type Set<?> is of Part
    super(form, field);
    mAnnotation = annotation;
    if (field.getType().equals(Part.class))
      mMultiple = false;
    else if (Set.class.isAssignableFrom(field.getType()))
      mMultiple = true;
    else
      throw new IllegalArgumentException(
          "FileFields must be of type Part or Set<Part>");

    mLabel = mAnnotation.label();
    mCssClass = mAnnotation.cssClass();
  }

  @Override
  public String render()
  {
    if (!isVisible())
      return "";

    String label = "";

    if (!"".equals(mAnnotation.label()))
      label = String.format("<label for=\"%1$s\">%2$s</label>",
          mAnnotation.id(), mAnnotation.label());
    if (mMultiple)
      return String
          .format(
              "%1$s\n<input type=\"file\" id=\"%2$s\" name=\"%3$s\" multiple/>\n",
              label, mAnnotation.id(), mField.getName());
    else
      return String.format(
          "%1$s\n<input type=\"file\" id=\"%2$s\" name=\"%3$s\"/>\n",
          label, mAnnotation.id(), mField.getName());

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
      if (mField.getType().equals(Part.class))
      {
        mField.set(mForm, part);
      }
      else if (Set.class.isAssignableFrom(mField.getType()))
      {
        Set<Part> set = (Set<Part>)mField.get(mForm);
        set.add(part);
      }
      else
        throw new RuntimeException("Filefield type must be Part or Set<Part>");
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Can't access field: " + mField.getName();
      log.debug(message, e);
      throw new RuntimeException(message, e);
    }
  }

  @Override
  public String toString()
  {
    return "FileField [" + mField.getName() + "]";
  }

  private snap.forms.annotations.FileField mAnnotation;
  private boolean mMultiple;

}
