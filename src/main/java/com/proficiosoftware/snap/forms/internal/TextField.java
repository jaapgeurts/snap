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
            "<label for=\"%0$s\">%1$s</label>\n<input type=\"text\" id=\"%0$s\" name=\"%2$s\" value=\"%3$s\"><br/>\n",
            mId, mLabel, mName, value);
  }

}
