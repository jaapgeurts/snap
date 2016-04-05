package snap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
  final DateTimeFormatter mHttpDateFormat = DateTimeFormatter
      .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

  public StaticRoute(String contextPath, String path, String alias,
      String directory)
  {
    super(contextPath, alias, path);
    // TODO: merge the matching into the super class
    mLocation = directory;
    // Only allow GET, HEAD method for static media
    mHttpMethods = new HttpMethod[] { HttpMethod.GET, HttpMethod.HEAD };
  }

  @Override
  public RequestResult handleRoute(RequestContext context)
      throws IOException, ServletException, HttpMethodException
  {

    // TODO: add gzip compression

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
    Instant fileLastModified = Instant.ofEpochSecond(file.lastModified());
    String modifiedSince = context.getRequest().getHeader("If-Modified-Since");
    if (modifiedSince != null)
    {
      try
      {
        ZonedDateTime cachedFileDate = ZonedDateTime.parse(modifiedSince,
            mHttpDateFormat);
        // reset the file time to the same timezone as the cachedFileDate
        ZonedDateTime zdt = ZonedDateTime.ofInstant(fileLastModified,
            cachedFileDate.getZone());

        // the file is older so don't send it (by default always send)
        if (!zdt.isAfter(cachedFileDate))
          fileIsNewerThanRequested = false;
      }
      catch (DateTimeException | NumberFormatException e)
      {
        log.warn("Can't parse If-Modified-Since date: \"" + modifiedSince
            + "\" Sending file anyway.");
      }
    }

    String contentDisposition = "inline";
    if (fileIsNewerThanRequested)
    {

      // Set the mimetype first
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
        contentDisposition = accept != null && accepts(accept, mimetype)
            ? "inline" : "attachment";
      }

      response.setHeader("Accept-Ranges", "bytes");

      response.setBufferSize(TRANSFER_BUFFER_SIZE);

      // figure out the range we have to return
      List<Pair<Long, Long>> ranges = null;
      String boundaryToken = UUID.randomUUID().toString();
      String rangeHeader = context.getRequest().getHeader("Range");
      if (rangeHeader != null)
        ranges = parseRanges(rangeHeader, file.length());

      try
      {
        if (ranges != null)
        {
          MultiPartFile multiPartFile = new MultiPartFile(file, ranges,
              boundaryToken, mimetype);
          response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
          response.setContentLengthLong(multiPartFile.getContentLength());
          response.setContentType(
              "multipart/byteranges; boundary=" + boundaryToken);
          // Transfer the data.
          transferData(new MultiPartFileInputStream(multiPartFile),
              response.getOutputStream());

        }
        else
        {
          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType(mimetype);
          response.setContentLengthLong(file.length());
          response.setHeader("Last-Modified", mHttpDateFormat.format(
              ZonedDateTime.ofInstant(fileLastModified, ZoneId.of("GMT"))));
          response.setHeader("Content-Disposition",
              contentDisposition + ";filename=\"" + file.getName() + "\"");

          transferData(new FileInputStream(file), response.getOutputStream());

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

  private List<Pair<Long, Long>> parseRanges(String rangeHeader, long filesize)
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
    List<Pair<Long, Long>> list = new ArrayList<>();
    parts = ranges.split(",");
    for (String range : parts)
    {
      long first = -1, last = -1;
      Pair<Long, Long> pair;
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
      pair = new Pair<>(first, last);
      list.add(pair);
    }
    return list;
  }

  private void transferData(InputStream inputStream, OutputStream outputStream)
      throws IOException
  {
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    try
    {

      in = new BufferedInputStream(inputStream);
      out = new BufferedOutputStream(outputStream);
      byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
      int len = in.read(buffer);
      while (len != -1)
      {
        out.write(buffer, 0, len);
        len = in.read(buffer);
      }
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
        // This usually happens if the user closes the connection prematurely
        log.info("User disconnected prematurely. " + ioe.getMessage());
      }
    }

  }

  private File getFile(RequestContext context, String fileName)
      throws IOException
  {

    File file = null;
    String permittedPath;

    String path = context.getRequest().getServletContext()
        .getRealPath(mLocation);
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
      String actualPath = context.getRequest().getServletContext()
          .getRealPath(computedPath);
      if (actualPath == null)
      {
        throw new ResourceNotFoundException(
            "File \"" + fileName + "\" not found on local disk");
      }
      file = new File(actualPath);
      permittedPath = new File(
          context.getRequest().getServletContext().getRealPath(mLocation))
              .getCanonicalPath();
    }
    String canonicalPath = file.getCanonicalPath();

    // check if the path is inside the directory path
    if (!canonicalPath.startsWith(permittedPath))
    {
      log.warn("Requested file " + file.getPath()
          + " is not under permitted folder: " + permittedPath);
      throw new ResourceNotFoundException(
          "File \"" + file.getAbsolutePath() + "\" not found on local disk");
    }

    if (!file.exists())
    {
      String message = "File not found: " + file.getAbsolutePath();
      log.info(message);
      throw new ResourceNotFoundException(message);
    }

    if (file.isDirectory())
    {
      String message = "Attempt to access directory \"" + file.getAbsolutePath()
          + "\" as file.";
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
        || Arrays.binarySearch(acceptValues,
            toAccept.replaceAll("/.*$", "/*")) > -1
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
    return mLocation;
  }

  private String mLocation;
}
