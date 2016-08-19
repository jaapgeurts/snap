package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.SnapException;
import snap.forms.Form;
import snap.forms.FormField;

public abstract class FormFieldBase implements FormField
{
  final Logger log = LoggerFactory.getLogger(FormFieldBase.class);

  protected FormFieldBase()
  {
    mAttributes = new HashMap<>();
  }

  /**
   * Constructor
   *
   * @param form
   *          The form this field belongs to
   * @param field
   *          the field
   * @param fieldName
   *          The name of the field, includes the full path to the root of the
   *          form
   */
  public FormFieldBase(Form form, Field field, String fieldName)
  {
    this();
    mField = field;
    mForm = form;
    mHtmlId = fieldName.replace('.', '-').toLowerCase();
    mFieldName = fieldName;
  }

  @Override
  public abstract String render();

  @Override
  public abstract String render(Map<String, String> attributes);

  @Override
  public String getError()
  {
    return mErrorText;
  }

  @Override
  public void setError(String errorText)
  {
    mErrorText = errorText;
  }

  @Override
  public void clearError()
  {
    mErrorText = null;
  }

  @Override
  public boolean hasError()
  {
    return mErrorText != null;
  }

  public void setFieldValue(String[] values)
  {
    try
    {
      Object obj = getFieldOwner();
      if (values == null)
      {
        mField.set(obj, null);
      }
      else
      {
        if (values.length > 1)
        {
          log.warn("Possible hacking attempt! Expected one value for field '" + mFieldName + "' but found: "
              + values.length);
        }
        mField.set(obj, values[0]);
      }
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      String message = "Can't access field: " + mFieldName;
      log.debug(message, e);
      throw new SnapException(message, e);
    }
  }

  /**
   * Returns the owner object that owns this field.
   *
   * @return The object that owns the field. In most cases it will be the Form
   *         object.
   * @throws NoSuchFieldException
   *           Thrown when the field doesn't exist
   * @throws SecurityException
   *           Thrown when access to the field is denied
   * @throws IllegalArgumentException
   *           Throws in the field argument is wrong
   * @throws IllegalAccessException
   *           thrown when access to the field is denied
   */
  protected Object getFieldOwner()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
  {
    String[] paths = mFieldName.split("\\.");
    Object obj = mForm;

    // skip the last part since we are interested in the owner object of the
    // field
    for (int i = 0; i < paths.length - 1; i++)
    {
      String path = paths[i];
      Class<?> clazz = obj.getClass();
      Field field = clazz.getField(path);
      obj = field.get(obj);
      if (obj == null)
        throw new SnapException("Can't descend down path '" + mFieldName + "'. Field '" + path + "' is NULL");
    }

    return obj;
  }

  /**
   * Returns the value of the field represented as a string.
   *
   * @return The field.toString() value of the field
   */
  protected String getFieldValueString()
  {
    try
    {
      Object o = mField.get(getFieldOwner());
      if (o != null)
        return o.toString();
      else
        return "";
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      log.debug("Can't access value of form field: " + mFieldName, e);
      throw new SnapException("Form field " + mFieldName + " can't be accessed.", e);
    }
  }

  /**
   * Returns the value of the field
   *
   * @return The value of the field
   */
  protected Object getFieldValue()
  {
    try
    {
      Object o = mField.get(getFieldOwner());
      return o;
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      log.debug("Can't access value of form field: " + mFieldName, e);
      throw new SnapException("Form field " + mFieldName + " can't be accessed.", e);
    }
  }

  @Override
  public void setVisible(boolean visible)
  {
    mVisible = visible;
  }

  @Override
  public boolean isVisible()
  {
    return mVisible;
  }

  @Override
  public void setHtmlId(String htmlId)
  {
    mHtmlId = htmlId;
  }

  @Override
  public String getHtmlId()
  {
    return mHtmlId;
  }

  @Override
  public String getHtmlId(String which)
  {
    if (which == null || "".equals(which))
      return getHtmlId();

    throw new SnapException(
        String.format("The field %1$s does not support HtmlID for values", mField.getName()));
  }

  @Override
  public String getLabel()
  {
    return mForm.parseAnnotationString(mLabel);
  }

  @Override
  public String getLabel(String which)
  {
    if (which == null || "".equals(which))
      return getLabel();

    throw new SnapException(
        String.format("The field %1$s does not support labels for values", mField.getName()));
  }

  @Override
  public void setLabel(String label)
  {
    mLabel = label;
  }

  /**
   * Returns all possible field values for this field
   *
   * @return a list of strings
   */
  @Override
  public String[] getOptions()
  {
    return new String[0];
  }

  @Override
  public void addAttribute(String attrib, String value)
  {
    if (!"".equals(value))
      mAttributes.put(attrib, value);
  }

  @Override
  public String getAttribute(String attrib)
  {
    return mAttributes.get(attrib);
  }

  @Override
  public void removeAttribute(String attrib)
  {
    mAttributes.remove(attrib);
  };

  @Override
  public Map<String, String> getAttributes()
  {
    return mAttributes;
  }

  protected String mHtmlId;
  protected String mLabel;
  protected Field mField;
  protected String mFieldName;
  protected Form mForm;
  private String mErrorText = null;
  protected Map<String, String> mAttributes;
  protected boolean mVisible = true;

}
