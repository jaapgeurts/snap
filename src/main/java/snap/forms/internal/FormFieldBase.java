package snap.forms.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.smartcardio.ATR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
          log.warn("Possible hacking attempt! Expected one value for field '"
              + mField.getName() + "' but found: " + values.length);
        }
        mField.set(mForm, values[0]);
      }
    }
    catch (IllegalArgumentException | IllegalAccessException e)
    {
      String message = "Can't access field: " + mField.getName();
      log.debug(message, e);
      throw new RuntimeException(message, e);
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
      throw new RuntimeException("Form field " + mField.getName()
          + " can't be accessed.", e);
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
  public String getLabel()
  {
    return mLabel;
  }

  @Override
  public void setLabel(String label)
  {
    mLabel = label;
  }

  @Override
  public String getCssClass()
  {
    return mCssClass;
  }

  @Override
  public void setCssClass(String cssClass)
  {
    mCssClass = cssClass;
  }

  @Override
  public void addAttribute(String attrib, String value)
  {
    mAttributes.put(attrib, value);
  }

  @Override
  public String getAttribute(String attrib)
  {
    return mAttributes.get(attrib);
  }

  /**
   * Merges the in attributes with the field attributes. The in attributes take
   * priority over the field attributes
   * 
   * @param in
   *          Map of key value pairs to merge.
   * @param overwrite
   *          Overwrite the values of in into the object attributes. Values for
   *          the class attribute are concatenated by default
   */
  @Override
  public void mergeAttributes(Map<String, Object> in, boolean overwrite)
  {
    for (Entry<String, Object> e : in.entrySet())
    {
      String key = e.getKey();
      if (mAttributes.containsKey(key))
      {
        // If the key is class then append the new classes to the existing class
        if ("class".equals(key))
          mAttributes.put(key, mAttributes.get(key) + " "
              + e.getValue().toString());
        else if (overwrite)
          mAttributes.put(key, e.getValue().toString());
        // do nothing if overwrite is off
      }
      else
      {
        mAttributes.put(e.getKey(), e.getValue().toString());
      }
    }
  }

  protected String getHtmlAttributes()
  {

    return mAttributes.entrySet().stream()
        .map(e -> e.getKey() + "='" + e.getValue() + "'")
        .collect(Collectors.joining(" "));
  }

  protected String mLabel;
  protected String mCssClass;
  protected Field mField;
  protected Form mForm;
  private String mErrorText = null;
  protected Map<String, String> mAttributes;
  protected boolean mVisible = true;

}
