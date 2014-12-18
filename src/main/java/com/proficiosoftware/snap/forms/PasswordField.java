package com.proficiosoftware.snap.forms;

public class PasswordField extends TextField
{
  
  public PasswordField(String name, String label)
  {
    super(name,label,"");
  }
  
  @Override
  public String render()
  {
    return "<input type=\"password\" name=\"" + mName + "\" value=\"" + mValue
        + "\">";
  }

}
