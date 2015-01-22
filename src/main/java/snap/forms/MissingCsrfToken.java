package snap.forms;

public class MissingCsrfToken extends RuntimeException
{
  public MissingCsrfToken(String message)
  {
    super(message);
  }

  public MissingCsrfToken(String message, Throwable cause)
  {
    super(message, cause);
  }
}
