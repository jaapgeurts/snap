package snap.http;

/**
 * Redirects that can be sent.
 * 
 * @author Jaap Geurts
 *
 */
public enum RedirectType {
  /**
   * HTTP 301 Permanent redirect, allow method to change on next request.
   * SC_MOVED_PERMANENTLY
   */
  PERMANENT_ALLOW_CHANGE,
  /**
   * HTTP 302 Temporary redirect, allow method to change on next request.
   * SC_FOUND
   */
  TEMPORARY_ALLOW_CHANGE,
  /**
   * HTTP 303 Permanent redirect, force next request method GET. SC_SEE_OTHER
   */
  PERMANENT_FORCE_GET,
  /**
   * HTTP 307 Temporary redirect, force the method to be the same as the
   * current request. SC_TEMPORARY_REDIRECT
   */
  TEMPORARY_FORCE_SAME,
  /**
   * HTTP 308 Permanent redirect, force the method to be the same as the
   * current request.
   */
  PERMANENT_FORCE_SAME
}