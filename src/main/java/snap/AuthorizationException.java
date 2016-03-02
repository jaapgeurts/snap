package snap;

public class AuthorizationException extends SnapException
{
  public AuthorizationException(String message)
  {
    super(message);
  }

  public AuthorizationException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
