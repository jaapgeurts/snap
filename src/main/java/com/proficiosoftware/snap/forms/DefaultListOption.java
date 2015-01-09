package com.proficiosoftware.snap.forms;

public class DefaultListOption implements ListOption
{

  private String mValue;
  private String mText;

  public DefaultListOption(String value, String text)
  {
    mValue = value;
    mText = text;
  }

  @Override
  public String getValue()
  {
    return mValue;
  }

  @Override
  public String getText()
  {
    return mText;
  }

}
