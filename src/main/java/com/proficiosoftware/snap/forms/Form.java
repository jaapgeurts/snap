package com.proficiosoftware.snap.forms;

import java.util.HashMap;
import java.util.Map;

public class Form
{
  public Form()
  {
    mFields = new HashMap<String, FormField>();
  }

  public Form(Map<String, String> defaults)
  {
    for(Map.Entry<String, String> entry: defaults.entrySet())
    {
      FormField field = mFields.get(entry.getKey());
      if (field != null)
      {
        field.setValue(entry.getValue());
      }
    }
  }
  
  public String render()
  {
    StringBuilder builder =new StringBuilder();
    for (FormField field: mFields.values())
    {
      builder.append(field.render());
    }
    return builder.toString();
  }

  public void addField(FormField field)
  {
    mFields.put(field.mName, field);
  }

  private Map<String, FormField> mFields;
}
