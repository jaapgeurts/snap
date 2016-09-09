package snap.rythm;

import org.rythmengine.template.JavaTagBase;

import snap.Router;

public class DynamicRootLink extends JavaTagBase
{

  public DynamicRootLink()
  {
  }

  @Override
  public String __getName()
  {
    return "rootlink";
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
      p(router.siteUri().toString() + router.linkForRoute(alias, plist));
    }
  }
}
