package snap;

public class InvalidCsrfTokenException extends RuntimeException
{
  public InvalidCsrfTokenException(String message)
  {
    super(message);
  }

  public InvalidCsrfTokenException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
