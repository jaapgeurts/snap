package com.proficiosoftware.snap.forms;

public class FileField extends FormField
{

  public FileField(String name, String label)
  {
    super(name, label);
  }

  @Override
  public String render()
  {
    return "<input type=\"file\" name=\"" + mName + "\">";
  }

}
