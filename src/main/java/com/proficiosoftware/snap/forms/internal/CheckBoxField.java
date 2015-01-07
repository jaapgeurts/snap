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
    String label = "";

    if (!"".equals(mLabel))
      label = String.format("<label for=\"%1$s\">%2$s</label>", mId, mLabel);
    if (Boolean.valueOf(value))
      return String
          .format(
              "%1$s\n<input type=\"checkbox\" id=\"%2$s\" name=\"%3$s\" value=\"%3$s\" checked>%5$<br/>\n",
              label, mId, mName);
    else
      return String
          .format(
              "%1$s\n<input type=\"checkbox\" id=\"%2$s\" name=\"%3$s\" value=\"%3$s\">%5$<br/>\n",
              label, mId, mName);

  }

}
