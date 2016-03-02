package snap;

public class ResourceNotFoundException extends SnapException
{
  public ResourceNotFoundException(String message)
  {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
