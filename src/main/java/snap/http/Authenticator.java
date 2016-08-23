package snap.http;

public interface Authenticator
{

  /**
   * return true if your authenticator can process this authentication request.
   * After this authenticate() will be called. you should match the first part
   * of the header. ie. "Basic" or "Bearer"
   *
   * @param header
   *          The header as sent by the client.
   * @return true if matched
   */
  boolean matchAuthenticationHeader(String header);

  /**
   * Returns the string to be included in the authentication header
   *
   * @return the header to return
   */
  String getWWWAuthenticateHeader();

  /**
   * perform authentication
   *
   * @param header
   *          The header as sent by the client.
   * @param context
   *          the request context
   * @return true when successful false otherwise
   */
  boolean authenticate(RequestContext context, String header);
}
