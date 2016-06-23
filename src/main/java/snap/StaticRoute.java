package snap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.HttpMethod;
import snap.http.RequestContext;
import snap.http.RequestResult;
import snap.views.NullView;

public class StaticRoute extends Route
{

  private class Range
  {
    long start;
    long end;
    long length;

    public Range(long start, long end)
    {
      this.start = start;
      this.end = end;
      this.length = end - start + 1;
    }
  }

  private static final int TRANSFER_BUFFER_SIZE = 10240;
  final Logger log = LoggerFactory.getLogger(StaticRoute.class);
  final DateTimeFormatter mHttpDateFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z",
      Locale.US);

  public StaticRoute(String contextPath, String path, String alias, String directory)
  {
    super(contextPath, alias, path);

    mLocation = directory;
    // Only allow GET, HEAD method for static media
    mHttpMethods = new HttpMethod[] { HttpMethod.GET, HttpMethod.HEAD };
  }

  @Override
  public RequestResult handleRoute(RequestContext context)
      throws IOException, ServletException, HttpMethodException
  {

    RouteListener r = getRouteListener();
    if (r != null)
    {
      RequestResult res = r.onBeforeRoute(context);
      if (res != null)
        return res;
    }

    processRequest(context);

    if (r != null)
      r.onAfterRoute(context);

    return NullView.INSTANCE;
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
    return mLocation;
  }

  @Override
  public boolean isStatic()
  {
    return true;
  }

  private void processRequest(RequestContext context) throws IOException
  {
    boolean shouldSendData = true;
    if (context.getMethod() == HttpMethod.HEAD)
      shouldSendData = false;

    // TODO: add gzip compression

    // Fetch the actual file and serve it directly
    Pattern p = Pattern.compile(mPath);
    Matcher m = p.matcher(context.getRequestURI());
    String fileName = m.replaceAll("");

    // Get a File object pointing to the correct file.
    // will throw a resource not found exception if the file doesn't exit
    File file = getFile(context, fileName);

    // Now that we know the actual file, if the client sent the
    // "If-Modified-Since" header
    // only send the file a newer version is available.
    boolean fileIsNewerThanRequested = true; // by default send the file
    // this may happen the modified since header data parsing fails
    Instant fileLastModified = Instant.ofEpochSecond(file.lastModified());
    String modifiedSince = context.getRequest().getHeader("If-Modified-Since");
    if (modifiedSince != null)
    {
      try
      {
        ZonedDateTime cachedFileDate = ZonedDateTime.parse(modifiedSince, mHttpDateFormat);
        // reset the file time to the same timezone as the cachedFileDate
        ZonedDateTime zdt = ZonedDateTime.ofInstant(fileLastModified, cachedFileDate.getZone());

        // the file is older so don't send it (by default always send)
        if (!zdt.isAfter(cachedFileDate))
          fileIsNewerThanRequested = false;
      }
      catch (DateTimeException | NumberFormatException e)
      {
        log.warn("Can't parse If-Modified-Since date: \"" + modifiedSince + "\" Sending file anyway.");
      }
    }

    if (!fileIsNewerThanRequested)
    {
      // file is not newer than client version
      HttpServletResponse response = context.getResponse();
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      // TODO: send ETag and expires
      return;
    }

    // prepare for sending data

    String contentDisposition = "inline";

    // Set the mimetype first
    String mimetype = context.getRequest().getServletContext().getMimeType(file.getCanonicalPath());
    if (mimetype == null)
      mimetype = "application/octet-stream";

    if (mimetype.startsWith("text")) // assume all text is served as UTF-8
    {
      mimetype += "; charset=UTF-8";
    }
    else if (mimetype.startsWith("image"))
    {
      String accept = context.getRequest().getHeader("Accept");
      contentDisposition = accept != null && accepts(accept, mimetype) ? "inline" : "attachment";
    }

    HttpServletResponse response = context.getResponse();

    // figure out the range we have to return
    List<Range> ranges = null;
    String boundaryToken = UUID.randomUUID().toString();
    String rangeHeader = context.getRequest().getHeader("Range");
    if (rangeHeader != null)
    {
      if ((!rangeHeader.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$"))
          || ((ranges = parseRanges(rangeHeader, file.length())) == null))
      {
        response.setHeader("Content-Range", "bytes */");
        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        return;
      }
    }

    // have the client recheck the cache in 1 week (60*60*24*7)
    response.setHeader("Cache-Control", "private, no-transform, max-age=604800");
    // even though Expires is deprecated and replaced by max-age, use it for
    // compatibility
    response.setHeader("Expires", mHttpDateFormat.format(ZonedDateTime.now(ZoneId.of("GMT")).plusDays(7)));
    response.setHeader("Accept-Ranges", "bytes");
    response.setHeader("Last-Modified",
        mHttpDateFormat.format(ZonedDateTime.ofInstant(fileLastModified, ZoneId.of("GMT"))));
    response.setHeader("Content-Disposition", contentDisposition + ";filename=\"" + file.getName() + "\"");
    response.setBufferSize(TRANSFER_BUFFER_SIZE);

    RandomAccessFile srcFile = null;
    ServletOutputStream os = null;
    try
    {
      // open the file
      srcFile = new RandomAccessFile(file, "r");
      os = response.getOutputStream();

      if (ranges == null)
      {
        response.setContentLengthLong(file.length());
        response.setContentType(mimetype);
        response.setStatus(HttpServletResponse.SC_OK);
        if (shouldSendData)
          transferData(srcFile, os, 0, file.length());
      }
      else if (ranges.size() == 1)
      {
        Range range = ranges.get(0);
        response.setContentLengthLong(range.length);
        response.setContentType(mimetype);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + file.length());

        if (shouldSendData)
          transferData(srcFile, os, range.start, range.length);
      }
      else if (ranges.size() > 1)
      {

        // send as multipart.
        // generate random boundary token
        final String BOUNDARY_TOKEN = UUID.randomUUID().toString();
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setContentType("multipart/byteranges; boundary=" + boundaryToken);
        if (shouldSendData)
        {
          for (Range range : ranges)
          {
            os.println();
            os.println("--" + BOUNDARY_TOKEN);
            os.println("Content-Type: " + mimetype);
            os.println("Content-Range: bytes " + range.start + "-" + range.end + "/" + file.length());
            transferData(srcFile, os, range.start, range.length);
          }
          os.println();
          os.println("--" + BOUNDARY_TOKEN + "--");
        }
      }
    }
    catch (IOException ioe)
    {
      try
      {
        // test to see if this IOException happened because the remote
        // side closed the connection
        os.flush();
        os = null;
        throw ioe;
      }
      catch (IOException e)
      {
        log.debug("Http client disconnected prematurely.");
      }
    }
    finally
    {
      try
      {
        if (srcFile != null)
          srcFile.close();
        if (os != null)
          os.flush();
      }
      catch (IOException ioe)
      {
        // This usually happens if the user closes the connection prematurely
        log.debug("Http client disconnected prematurely.");
      }
    }
  }

  private List<Range> parseRanges(String rangeHeader, long filesize)
  {

    String unit, ranges;
    String[] parts = rangeHeader.split("=");
    unit = parts[0];
    ranges = parts[1];

    if (!"bytes".equals(unit))
    {
      log.warn("Range request unit '" + unit + "' not supported");
      return null;
    }
    List<Range> list = new ArrayList<>();
    parts = ranges.split(",");
    for (String range : parts)
    {
      long first = -1, last = -1;
      String[] beginend = range.split("-");
      String begin = beginend[0];
      if (beginend.length == 2)
      {
        String end = beginend[1];
        if (end.length() > 0)
          last = Integer.valueOf(end);
      }
      if (begin.length() > 0)
        first = Integer.valueOf(begin);
      if (first == -1)
      {
        first = filesize - last;
        last = filesize - 1;
      }
      else if (last == -1)
      {
        last = filesize - 1;
      }
      list.add(new Range(first, last));
    }
    return list;
  }

  private void transferData(RandomAccessFile file, OutputStream out, long start, long length)
      throws IOException
  {
    byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
    int len;

    if (length == file.length()) // send the whole file
    {
      while ((len = file.read(buffer)) > 0)
        out.write(buffer, 0, len);
    }
    else
    {
      file.seek(start);
      while ((len = file.read(buffer)) > 0)
      {
        length -= len;
        if (length < 0)
        { // we read more than we should have
          out.write(buffer, 0, len + (int)length);
          break;
        }
        else
        {
          out.write(buffer, 0, len);
        }
      }
    }
  }

  private File getFile(RequestContext context, String fileName) throws IOException
  {

    File file = null;
    String permittedPath;

    String path = context.getRequest().getServletContext().getRealPath(mLocation);
    if (path != null)
    {
      file = new File(path);
      if (file != null && file.isFile())
        // the static route is a link to a direct file
        return file;
    }
    // Static Routes can point to files relative to the servlet root folder or
    // an absolute path prepended by a path specified in the routes.conf
    String computedPath = mLocation + "/" + fileName;
    File staticLocation = new File(computedPath);
    // If the location is specified as absolute then fetch it there
    if (staticLocation.isAbsolute())
    {
      // route points to absolute path.
      file = new File(computedPath);
      permittedPath = mLocation;
    }
    else
    {
      // Convert a location on the webroot to a path on the filesystem.
      String actualPath = context.getRequest().getServletContext().getRealPath(computedPath);
      if (actualPath == null)
      {
        throw new ResourceNotFoundException("File \"" + fileName + "\" not found on local disk");
      }
      file = new File(actualPath);
      permittedPath = new File(context.getRequest().getServletContext().getRealPath(mLocation))
          .getCanonicalPath();
    }
    String canonicalPath = file.getCanonicalPath();

    // check if the path is inside the directory path
    if (!canonicalPath.startsWith(permittedPath))
    {
      log.warn("Requested file " + file.getPath() + " is not under permitted folder: " + permittedPath);
      throw new ResourceNotFoundException("File \"" + file.getAbsolutePath() + "\" not found on local disk");
    }

    if (!file.exists())
    {
      String message = "File not found: " + file.getAbsolutePath();
      log.info(message);
      throw new ResourceNotFoundException(message);
    }

    if (file.isDirectory())
    {
      String message = "Attempt to access directory \"" + file.getAbsolutePath() + "\" as file.";
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

  private String mLocation;
}
