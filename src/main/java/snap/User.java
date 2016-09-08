package snap;

/**
 * Implement this interface to store the user object in the system so that
 * the @LoginRequired annotation is supported
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
   *          The role to check
   * @return true if the user has this role, false otherwise
   */
  boolean hasRole(String role);

  /**
   * Implement this if you support user rights. Return false otherwise;
   *
   * @param right
   *          The right to check if the user has it.
   * @return true, when the user has this right. false otherwise
   */
  boolean hasPermission(String right);

}
