package snap.rythm;

import org.rythmengine.template.JavaTagBase;

public class LinebreaksToParagraph extends JavaTagBase
{

  @Override
  public String __getName()
  {
    return "toP";
  }
 
  @Override
  protected void call(__ParameterList params, __Body body)
  {
    Object o = params.getDefault();
    if (o != null)
    {
      String s = (String)o.toString();
      p("<p>" + s.trim().replaceAll("[\\r|\\n]+", "</p><p>") + "</p>");
    }
  }

}
