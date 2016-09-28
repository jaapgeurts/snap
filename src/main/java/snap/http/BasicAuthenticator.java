package snap.http;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.Pair;

public abstract class BasicAuthenticator implements Authenticator
{
  final Logger log = LoggerFactory.getLogger(BasicAuthenticator.class);

  @Override
  public boolean matchAuthenticationHeader(String header)
  {
    return header.startsWith("Basic");
  }

  @Override
  public String getWWWAuthenticateHeader()
  {
    return "Basic realm=\"snap\"";
  }

  /**
   * Authenticate the user with the credentials. Return a userid when successful
   * or null when failed authentication failed. The system will ask your app for
   * the user by calling WebApplication.getUser(Long userid) when needed
   *
   * @param context
   *          The request context
   * @param username
   *          The username to check
   * @param password
   *          The password to validate
   * @return the ID of the user that was authenticated or null
   */
  public abstract Long authenticateUser(RequestContext context, String username, String password);

  @Override
  public boolean authenticate(RequestContext context, String header)
  {
    if (header == null || "".equals(header))
      return false;

    try
    {
      Pair<String, String> credentials = decodeAuthentication(header);
      if (credentials == null)
        return false;

      Long userid = authenticateUser(context, credentials.getFirst(), credentials.getSecond());
      if (userid == null)
        return false;
      context.setAuthenticatedUser(userid);
      return true;
    }
    catch (Exception e)
    {
      log.error("Error during Basic authentication", e);
    }
    return false;
  }

  private Pair<String, String> decodeAuthentication(String header)
  {

    String[] parts = header.trim().split(" ");
    if (parts.length != 2)
    {
      log.error("Malformed authentication header. Expected \"Bearer <value>\"");
      return null;
    }

    String token = new String(Base64.getDecoder().decode(parts[1].trim()), StandardCharsets.UTF_8);
    String[] credentials = token.trim().split(":");
    if (credentials.length != 2)
    {
      log.warn("Authorization header value not in correct format. Expected 'aaaaa:bbbbb'");
      return null;
    }

    return new Pair<String, String>(credentials[0], credentials[1]);

  }

}
