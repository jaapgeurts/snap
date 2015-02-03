package snap.forms;

public class MissingCsrfTokenException extends RuntimeException
{
  public MissingCsrfTokenException(String message)
  {
    super(message);
  }

  public MissingCsrfTokenException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
