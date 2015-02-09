package snap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.HttpMethod;
import snap.http.RequestContext;
import snap.http.RequestResult;
import snap.views.NullView;

public class StaticRoute extends Route
{
  private static final int TRANSFER_BUFFER_SIZE = 10240;
  final Logger log = LoggerFactory.getLogger(StaticRoute.class);
  final DateTimeFormatter mHttpDateFormat = DateTimeFormatter.ofPattern(
      "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

  public StaticRoute(String contextPath, String path, String alias,
      String directory)
  {
    super(contextPath, alias, path);
    // TODO: merge the matching into the super class
    mDirectoryPrefix = directory;
    // Only allow GET, HEAD method for static media
    mHttpMethods = new HttpMethod[] { HttpMethod.GET, HttpMethod.HEAD };
  }

  @Override
  public RequestResult handleRoute(RequestContext context) throws IOException,
      ServletException, HttpMethodException
  {

    // TODO: add gzip compression & ranges

    RouteListener r = getRouteListener();
    if (r != null)
    {
      RequestResult res = r.onBeforeRoute(context);
      if (res != null)
        return res;
    }

    // Fetch the actual file and serve it directly
    Pattern p = Pattern.compile(mPath);
    Matcher m = p.matcher(context.getRequestURI());
    String fileName = m.replaceAll("");

    // Get a File object pointing to the correct file.
    File file = getFile(context, fileName);

    // Now that we know the actual file, if the client sent the
    // "If-Modified-Since" header
    // only send the file a newer version is available.
    boolean fileIsNewerThanRequested = true; // by default send the file
    // this may happen the modified since header data parsing fails
    ZonedDateTime fileDate = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(file.lastModified()), ZoneId.systemDefault());
    String modifiedSince = context.getRequest().getHeader("If-Modified-Since");
    if (modifiedSince != null)
    {
      try
      {
        ZonedDateTime cachedFileDate = ZonedDateTime.parse(modifiedSince,
            mHttpDateFormat);

        // the file is older so don't send it (by default always send) 
        if (fileDate.isBefore(cachedFileDate))
          fileIsNewerThanRequested = false;
      }
      catch (DateTimeParseException | NumberFormatException e)
      {
        log.warn("Can't parse If-Modified-Since date: \"" + modifiedSince
            + "\" Sending file anyway.", e);
      }
    }

    String contentDisposition = "inline";
    if (fileIsNewerThanRequested)
    {
      // Set the headers.
      String mimetype = context.getRequest().getServletContext()
          .getMimeType(file.getCanonicalPath());
      if (mimetype == null)
        mimetype = "application/octet-stream";

      HttpServletResponse response = context.getResponse();

      if (mimetype.startsWith("text")) // assume all text is served as UTF-8
      {
        mimetype += "; charset=UTF-8";
      }
      else if (mimetype.startsWith("image"))
      {
        String accept = context.getRequest().getHeader("Accept");
        contentDisposition = accept != null && accepts(accept, mimetype) ? "inline"
            : "attachment";
      }
      response.setContentType(mimetype);

      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentLengthLong(file.length());
      response.setHeader("Last-Modified", mHttpDateFormat.format(fileDate));
      response.setHeader("Content-Disposition", contentDisposition
          + ";filename=\"" + file.getName() + "\"");

      response.setBufferSize(TRANSFER_BUFFER_SIZE);
      // Transfer the data.
      transferData(file, response.getOutputStream());
    }
    else
    {
      // file is not newer than client version
      HttpServletResponse response = context.getResponse();
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }
    if (r != null)
      r.onAfterRoute(context);

    return NullView.INSTANCE;
  }

  private void transferData(File file, OutputStream outputStream)
      throws IOException
  {
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    try
    {

      in = new BufferedInputStream(new FileInputStream(file));
      out = new BufferedOutputStream(outputStream);
      byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
      int len = in.read(buffer);
      while (len != -1)
      {
        out.write(buffer, 0, len);
        len = in.read(buffer);
      }
    }
    catch (FileNotFoundException fnfe)
    {
      String message = "File not found after File.exists(): "
          + file.getAbsolutePath();
      log.info(message);
      throw new ResourceNotFoundException(message, fnfe);
    }
    catch (IOException e)
    {
      log.error("Error serving file", e);
      throw e;
    }
    finally
    {
      try
      {
        if (out != null)
          out.flush();
        if (in != null)
          in.close();
      }
      catch (IOException ioe)
      {
        // report this IOException but still serve the file
        log.warn("Error flushing output or closing input.", ioe);
      }
    }

  }

  private File getFile(RequestContext context, String fileName)
      throws IOException
  {

    File file = null;
    String permittedPath;

    // Static Routes can point to files relative to the servlet root folder or
    // an absolute path prepended by a path specified in the routes.conf
    String computedPath = mDirectoryPrefix + "/" + fileName;
    File staticLocation = new File(computedPath);
    if (staticLocation.isAbsolute())
    {
      // route points to absolute path.
      file = new File(computedPath);
      permittedPath = mDirectoryPrefix;
    }
    else
    {
      // Convert a path on the webroot to a file on the filesystem path.
      String actualPath = context.getRequest().getServletContext()
          .getRealPath(computedPath);
      if (actualPath == null)
      {
        throw new ResourceNotFoundException("File \"" + fileName
            + "\" not found on local disk");
      }
      file = new File(actualPath);
      permittedPath = new File(context.getRequest().getServletContext()
          .getRealPath(mDirectoryPrefix)).getCanonicalPath();
    }
    String canonicalPath = file.getCanonicalPath();

    // check if the path is inside the directory path
    if (!canonicalPath.startsWith(permittedPath))
    {
      log.warn("Requested file " + file.getPath()
          + " is not under permitted folder: " + permittedPath);
      throw new ResourceNotFoundException("File \"" + file.getAbsolutePath()
          + "\" not found on local disk");
    }

    if (!file.exists())
    {
      String message = "File not found: " + file.getAbsolutePath();
      log.info(message);
      throw new ResourceNotFoundException(message);
    }

    if (file.isDirectory())
    {
      String message = "Attempt to access directory: " + file.getAbsolutePath();
      log.info(message);
      throw new ResourceNotFoundException(message);
    }

    if (!file.canRead())
    {
      String message = "File can't be read: " + file.getAbsolutePath();
      log.info(message);
      throw new ResourceNotFoundException(message);
    }

    return file;
  }

  // TODO: rewrite this code
  private static boolean accepts(String acceptHeader, String toAccept)
  {
    String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
    Arrays.sort(acceptValues);
    return Arrays.binarySearch(acceptValues, toAccept) > -1
        || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
        || Arrays.binarySearch(acceptValues, "*/*") > -1;
  }

  @Override
  public String getLink(Object[] params)
  {
    int begin = 0, end = mPath.length();
    if (mPath.charAt(0) == '^')
      begin++;
    if (mPath.charAt(end - 1) == '$')
      end--;
    String path = mPath.substring(begin, end);
    if (mContextPath == null || "".equals(mContextPath))
      return path + params[0].toString();
    else
      return mContextPath + path + params[0].toString();
  }

  public String getDirectory()
  {
    return mDirectoryPrefix;
  }

  private String mDirectoryPrefix;
}
