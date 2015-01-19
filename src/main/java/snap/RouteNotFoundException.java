package snap;

public class RouteNotFoundException extends ResourceNotFoundException
{
  public RouteNotFoundException(String message)
  {
    super(message);
  }

  public RouteNotFoundException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
