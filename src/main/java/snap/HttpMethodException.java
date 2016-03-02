package snap;

public class HttpMethodException extends SnapException
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
