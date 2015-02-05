package snap;

public class HttpMethodException extends RuntimeException
{
  public HttpMethodException(String message)
  {
    super(message);
  }

  public HttpMethodException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
