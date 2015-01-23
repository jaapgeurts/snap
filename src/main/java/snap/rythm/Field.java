package snap.rythm;

import org.rythmengine.template.JavaTagBase;

public class Field extends JavaTagBase
{

  public Field()
  {
  }

  @Override
  public String __getName()
  {
    return "field";
  }

  @Override
  protected void call(__ParameterList params, __Body body)
  {
    Object o = params.getDefault();
    if (o != null && o instanceof snap.forms.Form)
    {
      snap.forms.Form form = (snap.forms.Form)o;
      o = params.getByPosition(1);
      if (o == null)
        // TODO: add logging
        return;
      String name = o.toString();
      o = params.getByPosition(2);
      if (o != null)
        p(form.renderField(name, o));
      else
        p(form.renderField(name));
    }
  }
}
