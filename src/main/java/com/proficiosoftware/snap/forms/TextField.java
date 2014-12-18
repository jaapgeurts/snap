package com.proficiosoftware.snap.forms;

public class TextField extends FormField
{
  

  public TextField(String name, String label, String value)
  {
    super(name,label);
    setValue(value);
  }
  
  @Override
  public String render()
  {
    return "<input type=\"text\" name=\"" + mName + "\" value=\"" + mValue
        + "\">";
  }

}
