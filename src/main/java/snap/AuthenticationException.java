package snap;

public class AuthenticationException extends SnapException
{

  public AuthenticationException(String message)
  {
    super(message);
  }

  public AuthenticationException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
