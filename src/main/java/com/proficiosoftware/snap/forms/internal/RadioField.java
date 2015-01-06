package com.proficiosoftware.snap.forms.internal;

public class RadioField<E extends Enum<E>> extends FormField
{

  public RadioField(String id, String name, Class<E> enumClass)
  {
    super(id, name, "");
    mEnum = enumClass;
  }

  @Override
  public String render(String value)
  {
    StringBuilder b = new StringBuilder();
    for (Enum<E> enumVal : mEnum.getEnumConstants())
    {
      String val = enumVal.toString();
      if (val.equals(value))
        b.append(String
            .format(
                "<input type=\"radio\" name=\"%1$s\" value=\"%2$s\" checked>%2$s</input>",
                mName, val));
      else
        b.append(String.format(
            "<input type=\"radio\" name=\"%1$s\" value=\"%2$s\">%2$s</input>",
            mName, val));

    }
    return b.toString();
  }

  private Class<E> mEnum;

}
