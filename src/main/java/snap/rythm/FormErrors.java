package snap.rythm;

import org.rythmengine.template.JavaTagBase;

public class FormErrors extends JavaTagBase
{

  public FormErrors()
  {
  }

  @Override
  public String __getName()
  {
    return "formerrors";
  }

  @Override
  protected void call(__ParameterList params, __Body body)
  {
    Object o = params.getDefault();
    if (o != null && o instanceof snap.forms.Form)
    {
      snap.forms.Form form = (snap.forms.Form)o;
      p(form.renderErrors());
    }
  }
}
