package snap.rythm;

import org.rythmengine.template.JavaTagBase;

import snap.Router;

public class DynamicRelativeLink extends JavaTagBase
{

  public DynamicRelativeLink()
  {
  }

  @Override
  public String __getName()
  {
    return "link";
  }

  @Override
  protected void call(__ParameterList params, __Body body)
  {
    Router router = Router.getInstance();
    Object o = params.getDefault();
    if (o != null)
    {
      String alias = o.toString();
      Object[] plist = new Object[params.size() - 1];
      for (int i = 0; i < plist.length; i++)
        plist[i] = params.get(i + 1).value;
      p(router.linkForRoute(alias, plist));
    }
  }
}
