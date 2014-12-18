package com.proficiosoftware.snap.forms;

public class SubmitButton extends FormField
{

  public SubmitButton(String name, String label, String value)
  {
    super(name,label);
    setValue(value);
  }
   
  @Override
  public String render()
  {
    return "<input type=\"submit\" value=\""+mValue+"\" name=\""+mName+"\">";
  }

}
