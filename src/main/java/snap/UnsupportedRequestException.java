package snap;

public class UnsupportedRequestException extends SnapException
{

  public UnsupportedRequestException(String message)
  {
    super(message);
  }

  public UnsupportedRequestException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
