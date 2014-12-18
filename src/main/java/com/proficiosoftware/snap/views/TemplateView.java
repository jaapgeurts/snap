package com.proficiosoftware.snap.views;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;

import com.proficiosoftware.snap.Router;

public class TemplateView extends View
{

  public TemplateView(String templateName)
  {
    mTemplateName = templateName;
    mContext = new HashMap<String, Object>();
    mContext.put("router", Router.instance());
  }

  public void addParameter(String name, Object value)
  {
    mContext.put(name, value);
  }

  @Override
  public CharSequence render()
  {
    File file = new File(mTemplateName);
    if (!file.exists())
      throw new RuntimeException("Can't open template: " + mTemplateName);
    RythmEngine r = Rythm.engine();
    return Rythm.render(file, mContext);
  }

  private String mTemplateName;
  protected Map<String, Object> mContext;

}
