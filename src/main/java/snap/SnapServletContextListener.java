package snap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.annotation.WebListener;

@WebListener
public class SnapServletContextListener implements ServletContextListener
{

  @Override
  public void contextInitialized(ServletContextEvent sce)
  {

    // force loading of settings so that the static initializer is called
    try
    {
      Class.forName("snap.Settings");
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new IllegalStateException("Can't load settings", cnfe);
    }

    SessionCookieConfig scc = sce.getServletContext().getSessionCookieConfig();
    scc.setComment("Snap Session Cookie Config");
    scc.setName(Settings.get("snap.session.cookie.name", "JSESSIONID"));
    String domain = Settings.get("snap.session.cookie.domain", Settings.siteRootUri.getHost());
    if (!"localhost".equals(domain) && !".localhost".equals(domain))
      scc.setDomain(domain);
    scc.setMaxAge(Settings.getInt("snap.session.cookie.max-age", -1));
    scc.setPath(Settings.get("snap.session.cookie.path", "/"));

  }

  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {

  }

}
