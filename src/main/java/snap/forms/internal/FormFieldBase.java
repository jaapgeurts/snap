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
   * @param field
   */
  public FormFieldBase(Form form, Field field)
  {
    this();
    mField = field;
    mForm = form;
  }

  public abstract String render();

  public abstract String render(Map<String, String> attributes);

  public String getError()
  {
    return mErrorText;
  }

  public void setError(String errorText)
  {
    mErrorText = errorText;
  }

  public void clearError()
  {
    mErrorText = null;
  }

  public boolean hasError()
  {
    return mErrorText != null;
  }

  public void setFieldValue(String[] values)
  {
    try
    {
      if (values == null)
      {
        mField.set(mForm, null);
      }
      else
      {
        if (values.length > 1)
        {
          log.warn("Possible hacking attempt! Expected one value for field '" + mField.getName()
              + "' but found: " + values.length);
        }
        mField.set(mForm, values[0]);
      }
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Can't access field: " + mField.getName();
      log.debug(message, e);
      throw new SnapException(message, e);
    }
  }

  protected String getFieldValue()
  {
    try
    {
      Object o = mField.get(mForm);
      if (o != null)
        return o.toString();
      else
        return "";
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      log.debug("Can't access value of form field: " + mField.getName(), e);
      throw new SnapException("Form field " + mField.getName() + " can't be accessed.", e);
    }
  }

  public void setVisible(boolean visible)
  {
    mVisible = visible;
  }

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
    return mLabel;
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
  protected Form mForm;
  private String mErrorText = null;
  protected Map<String, String> mAttributes;
  protected boolean mVisible = true;

}
