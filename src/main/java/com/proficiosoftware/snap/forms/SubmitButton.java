package com.proficiosoftware.snap.forms;

import com.proficiosoftware.snap.forms.internal.FormField;

public class SubmitButton extends FormField
{

  public SubmitButton(String id, String name)
  {
    super(id, name, "");
  }

  @Override
  public String render(String value)
  {
    return String
        .format(
            "<input type=\"submit\" id=\"%0$s\" name=\"%2$s\" value=\"%3$s\"><br/>",
            mId, mLabel, mName, value);
  }

}
