package snap.forms;

public class InvalidCsrfToken extends RuntimeException
{
  public InvalidCsrfToken(String message)
  {
    super(message);
  }

  public InvalidCsrfToken(String message, Throwable cause)
  {
    super(message, cause);
  }
}
