package com.proficiosoftware.snap;

public class UnauthorizedAccessException extends Exception
{

  public UnauthorizedAccessException(String message)
  {
    super(message);
  }

  public UnauthorizedAccessException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
