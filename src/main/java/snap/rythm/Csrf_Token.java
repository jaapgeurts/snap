package snap.rythm;

import org.rythmengine.template.JavaTagBase;

public class Csrf_Token extends JavaTagBase
{

  public Csrf_Token()
  {
  }

  @Override
  public String __getName()
  {
    return "csrf_token";
  }

  @Override
  protected void call(__ParameterList params, __Body body)
  {

    Object token = __getRenderArg("csrf_token_value");
    if (token == null)
      token = "";
    p(String.format("<input type=\"hidden\" name=\"csrf_token\" value=\"%s\"/>", token));
  }
}
