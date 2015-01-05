package com.proficiosoftware.snap.forms.internal;

import java.util.List;

import com.proficiosoftware.snap.forms.ListOption;

public class DropDownList extends FormField
{

  public DropDownList(String id, String name, String label, List<?> options)
  {
    super(id, name, label);
    if (options.size() > 0)
    {
      mList = options;
    }
  }

  @Override
  public String render(String value)
  {
    StringBuilder b = new StringBuilder();

    if (!"".equals(mLabel))
      b.append(String.format("<label for=\"%1$s\">%2$s</label>", mId, mLabel));
    b.append(String.format(
        "\n<select multiple=\"false\" id=\"%1$s\" name=\"%2$s\"><br/>\n", mId,
        mName));
    for (Object o : mList)
    {
      String val, text;
      if (o instanceof ListOption)
      {
        ListOption lo = (ListOption)o;
        val = lo.getValue();
        text = lo.getText();
      }
      else
      {
        val = text = o.toString();
      }
      if (val.equals(value))
        b.append(String.format(
            "\t<option selected value=\"%1$d\">%2$d</option>\n", val, text));
      else
        b.append(String.format("\t<option value=\"%1$d\">%2$d</option>\n", val,
            text));
    }
    b.append("</select>");
    return b.toString();
  }

  private List<?> mList = null;
}
