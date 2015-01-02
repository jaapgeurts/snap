package com.proficiosoftware.snap.forms.internal;

public class TextArea extends FormField
{

  public TextArea(String id, String name, String label)
  {
    super(id, name, label);
  }

  @Override
  public String render(String value)
  {
    return String
        .format(
            "<label for=\"%1$s\">%2$s</label>\n<textarea id=\"%1$s\" name=\"%3$s\">%4$s</textarea>\n",
            mId, mLabel, mName, value);

  }

}
