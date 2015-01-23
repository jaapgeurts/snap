package snap.forms.internal;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.forms.Form;
import snap.forms.FormField;

public abstract class FormBase implements FormField
{
  final Logger log = LoggerFactory.getLogger(FormBase.class);

  protected FormBase()
  {
  }

  public FormBase(Form form, Field field)
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
    mCssClass = cssClass;;
  }

  protected String mLabel;
  protected String mCssClass;
  protected Field mField;
  protected Form mForm;
  private String mErrorText = null;
  protected boolean mVisible = true;

}
