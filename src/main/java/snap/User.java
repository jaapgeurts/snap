package snap;

/**
 * Implement this interface to store the user object in the system so that the @LoginRequired
 * annotation is supported
 * 
 * @author Jaap Geurts
 *
 */
public interface User
{
  /**
   * Implement this if you support roles. return false otherwise;
   * 
   * @param role
   * @return
   */
  public boolean hasRole(String role);

  /**
   * Implement this if you support user rights. Return false otherwise;
   * 
   * @param role
   * @return
   */
  public boolean hasRight(int right);
}
