package com.proficiosoftware.snap.forms;

public abstract class FormField
{

  protected FormField()
  {
  }

  public FormField(String name, String label)
  {
    mName = name;
    mLabel = label;
  }

  public abstract String render();

  public String getValue()
  {
    return mValue;
  }

  public void setValue(String value)
  {
    // TODO copy array
    mValue = value;
  }

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

  protected String mName;
  protected String mLabel;
  protected String mValue;

}
