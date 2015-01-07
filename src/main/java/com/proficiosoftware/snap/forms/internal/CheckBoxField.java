package com.proficiosoftware.snap.forms.internal;

public class CheckBoxField extends FormField
{

  public CheckBoxField(String id, String name, String label)
  {
    super(id, name, label);
  }

  @Override
  public String render(String value)
  {
    String labelPre = "";
    String labelPost = "";

    if (!"".equals(mLabel))
    {
      labelPre = String.format("<label for=\"%1$s\">", mId);
      labelPost = String.format("%1$s</label>", mLabel);
    }
    if (Boolean.valueOf(value))
      return String
          .format(
              "%1$s\n<input type=\"checkbox\" id=\"%2$s\" name=\"%3$s\" value=\"%3$s\" checked>%4$s<br/>\n",
              labelPre, mId, mName, labelPost);
    else
      return String
          .format(
              "%1$s\n<input type=\"checkbox\" id=\"%2$s\" name=\"%3$s\" value=\"%3$s\">%4$s<br/>\n",
              labelPre, mId, mName, labelPost);

  }

}
