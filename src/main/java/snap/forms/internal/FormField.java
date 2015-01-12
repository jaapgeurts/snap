package snap.forms.internal;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.forms.Form;

public abstract class FormField
{
  final Logger log = LoggerFactory.getLogger(FormField.class);

  protected FormField()
  {
  }

  public FormField(Form form, Field field)
  {
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
          log.warn("Possible hacking attempt! Expected one value for field \""
              + mField.getName() + "\" but found: " + values.length);
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
        return toString();
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

  protected Field mField;
  protected Form mForm;
  private String mErrorText = null;

}
