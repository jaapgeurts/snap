package com.proficiosoftware.snap;

public class HttpMethodException extends Exception
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
