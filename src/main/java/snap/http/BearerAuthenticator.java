package snap.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BearerAuthenticator implements Authenticator
{
  final Logger log = LoggerFactory.getLogger(BearerAuthenticator.class);

  @Override
  public boolean matchAuthenticationHeader(String header)
  {
    return header.startsWith("Bearer");
  }

  @Override
  public String getWWWAuthenticateHeader()
  {
    return "Bearer realm=\"snap\"";
  }

  /**
   * Authenticate the user with the credentials. Return a userid when successful
   * or null when failed authentication failed. The system will ask your app for
   * the user by calling WebApplication.getUser(Long userid) when needed
   * 
   * @param context
   *          The request context
   * @param token
   *          The access token
   * @return the ID of the user that was authenticated or null
   */
  public abstract Long authenticateUser(RequestContext context, String token);

  @Override
  public boolean authenticate(RequestContext context, String header)
  {
    if (header == null || "".equals(header))
      return false;

    String[] parts = header.trim().split(" ");
    if (parts == null || parts.length != 2)
    {
      log.error("Malformed authentication header. Expected \"Bearer <value>\"");
      return false;
    }

    Long userid = authenticateUser(context, parts[1]);
    if (userid == null)
      return false;
    context.setAuthenticatedUser(userid);
    return true;
  }

}
