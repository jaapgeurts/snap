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
    String label = "";

    if (!"".equals(mLabel))
      label = String.format("<label for=\"%1$s\">%2$s</label>", mId, mLabel);
    return String.format(
        "%1$s<textarea id=\"%2$s\" name=\"%3$s\">%4$s</textarea>\n", label,
        mId, mName, value);

  }

}
