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
    // Ignore the value parameter:: never set passwords in HTML
    return String
        .format(
            "<label for=\"%1$s\">%2$s</label><input type=\"password\" id=\"%1$s\" name=\"%3$s\"><br/>",
            mId, mLabel, mName);
  }

}
