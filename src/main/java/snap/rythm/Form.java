package snap.rythm;

import org.rythmengine.template.JavaTagBase;

public class Form extends JavaTagBase
{

  public Form()
  {
  }

  @Override
  public String __getName()
  {
    return "form";
  }

  @Override
  protected void call(__ParameterList params, __Body body)
  {
    Object o = params.getDefault();
    if (o != null && o instanceof snap.forms.Form)
    {
      snap.forms.Form form = (snap.forms.Form)o;
      o = params.getByPosition(1);
      String type = null;
      if (o != null)
        type = o.toString();
      else
        type = "div";
      p(form.render(type));
    }
  }
}
