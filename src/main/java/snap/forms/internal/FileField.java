package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Part;

import snap.Helpers;
import snap.SnapException;
import snap.forms.Form;

public class FileField extends FormFieldBase
{

  public FileField(Form form, Field field, snap.forms.annotations.FileField annotation, String fieldName)
  {
    super(form, field, fieldName);
    mAnnotation = annotation;
    if (field.getType().equals(Part.class))
      mMultiple = false;
    else if (Set.class.isAssignableFrom(field.getType()))
      mMultiple = true;
    else
      throw new IllegalArgumentException("FileFields must be of type Part or Set<Part>");

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

    if (mMultiple)
      return String.format("<input type='file' id='%1$s' name='%2$s' multiple %3$s/>\n", mHtmlId, mFieldName,
          Helpers.attrToString(attributes));
    else
      return String.format("<input type='file' id='%1$s' name='%2$s' %3$s/>\n", mHtmlId, mFieldName,
          Helpers.attrToString(attributes));

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
        mField.set(getFieldOwner(), part);
      }
      else if (Set.class.isAssignableFrom(mField.getType()))
      {
        Set<Part> set = (Set<Part>)mField.get(getFieldOwner());
        set.add(part);
      }
      else
        throw new SnapException("Filefield type must be Part or Set<Part>");
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      String message = "Can't access field: " + mFieldName;
      log.debug(message, e);
      throw new SnapException(message, e);
    }
  }

  @Override
  public String toString()
  {
    return "FileField { " + mFieldName + " }";
  }

  private snap.forms.annotations.FileField mAnnotation;
  private boolean mMultiple;

}
