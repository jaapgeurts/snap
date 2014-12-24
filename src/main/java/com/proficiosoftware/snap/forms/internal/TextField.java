package com.proficiosoftware.snap.forms.internal;

public class TextField extends FormField
{

  public TextField(String id, String name, String label)
  {
    super(id, name, label);
  }

  @Override
  public String render(String value)
  {
    return String
        .format(
            "<label for=\"%1$s\">%2$s</label>\n<input type=\"text\" id=\"%1$s\" name=\"%3$s\" value=\"%4$s\"><br/>\n",
            mId, mLabel, mName, value);
  }

}
