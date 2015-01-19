package snap;

public class SnapException extends RuntimeException
{
  public SnapException(String message)
  {
    super(message);
  }

  public SnapException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
