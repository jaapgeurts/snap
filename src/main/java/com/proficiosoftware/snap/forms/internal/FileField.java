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
            "<label for=\"%0$s\">%1$s</label>\n<input type=\"file\" id=\"%0$s\" name=\"%2$s\"><br/>",
            mId, mLabel, mName);
  }

}
