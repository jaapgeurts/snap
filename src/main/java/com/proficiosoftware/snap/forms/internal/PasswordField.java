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
    String label = "";

    if (!"".equals(mLabel))
      label = String.format("<label for=\"%1$s\">%2$s</label>", mId, mLabel);
    return String.format(
        "%1$s<input type=\"password\" id=\"%2$s\" name=\"%3$s\"><br/>", label,
        mId, mName);
  }

}
