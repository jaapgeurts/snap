package com.proficiosoftware.snap;

public class AuthorizationException extends Exception
{
  public AuthorizationException(String message)
  {
    super(message);
  }

  public AuthorizationException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
