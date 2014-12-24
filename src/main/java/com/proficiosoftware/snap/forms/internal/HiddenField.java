package com.proficiosoftware.snap.forms.internal;

public class HiddenField extends FormField
{

  public HiddenField(String id, String fieldName)
  {
    super(id, fieldName, "");
  }

  @Override
  public String render(String value)
  {
    return String.format(
        "<input type=\"hidden\" id=\"%1$s\" name=\"%2$s\" value=\"%3$s\">",
        mId, mName, value);
  }

}
