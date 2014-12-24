package com.proficiosoftware.snap.forms.internal;

public abstract class FormField
{

  protected FormField()
  {
  }

  public FormField(String id, String name, String label)
  {
    mId = id;
    mName = name;
    mLabel = label;
  }

  public abstract String render(String value);

  public String getName()
  {
    return mName;
  }

  public void setName(String name)
  {
    mName = name;
  }

  public String getLabel()
  {
    return mLabel;
  }

  public void setLabel(String label)
  {
    mLabel = label;
  }

  public String getId()
  {
    return mId;
  }

  public void setId(String mId)
  {
    this.mId = mId;
  }

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

  protected String mId;
  protected String mName;
  protected String mLabel;
  private String mErrorText = null;

}
