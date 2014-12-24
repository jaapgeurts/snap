package com.proficiosoftware.snap.forms.internal;

public class FileField extends FormField
{

  public FileField(String id, String name, String label)
  {
    super(id, name, label);
  }

  @Override
  public String render(String value)
  {
    return String
        .format(
            "<label for=\"%1$s\">%2$s</label>\n<input type=\"file\" id=\"%1$s\" name=\"%3$s\"><br/>",
            mId, mLabel, mName);
  }

}
