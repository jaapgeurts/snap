package com.proficiosoftware.snap.forms.internal;

public class PasswordField extends TextField
{

  public PasswordField(String id, String name, String label)
  {
    super(id, name, label);
  }

  @Override
  public String render(String value)
  {
    return String
        .format(
            "<label for=\"%0$s\">%1$s</label><input type=\"password\" id=\"%0$s\" name=\"%2$s\" value=\"%3$s\"><br/>",
            mId, mLabel, mName, value);
  }

}
