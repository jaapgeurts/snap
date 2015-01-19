package snap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.http.HttpMethod;
import snap.http.HttpNull;
import snap.http.RequestContext;
import snap.http.RequestResult;

public class StaticRoute extends Route
{
  final Logger log = LoggerFactory.getLogger(StaticRoute.class);

  public StaticRoute(String contextPath, String path, String alias,
      String directory)
  {
    super(contextPath, alias, path);
    // TODO: merge the matching into the super class
    mDirectory = directory;
    // Only allow GET method for static media
    mHttpMethod = HttpMethod.GET;
  }

  @Override
  public RequestResult handleRoute(RequestContext context) throws IOException
  {
    // Fetch the actual file and serve it directly
    Pattern p = Pattern.compile(mPath);
    Matcher m = p.matcher(context.getRequest().getPathInfo());
    String file = m.replaceAll("");
    String extension = getExtension(file);
    String finalFile = mDirectory + "/" + file;
    File f = new File(finalFile);

    if (!f.isAbsolute())
      f = new File(context.getRequest().getServletContext()
          .getRealPath(finalFile));

    // TODO: if possible forward serving the file to the Servlet container

    BufferedInputStream in;
    BufferedOutputStream out;
    try
    {
      HttpServletResponse response = context.getResponse();

      response.setContentType(getMimeType(extension));
      response.setStatus(HttpServletResponse.SC_OK);

      in = new BufferedInputStream(new FileInputStream(f));
      out = new BufferedOutputStream(response.getOutputStream());
      byte[] buffer = new byte[2048];
      int len = in.read(buffer);
      while (len != -1)
      {
        out.write(buffer, 0, len);
        len = in.read(buffer);
      }
      out.flush();
      in.close();
    }
    catch (FileNotFoundException fnfe)
    {
      String message = "File not found: " + mDirectory + "/" + file;
      log.info(message);
      throw new ResourceNotFoundException(message, fnfe);
    }
    catch (IOException e)
    {
      log.error("Error serving file", e);
      throw e;
    }
    return HttpNull.INSTANCE;
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
    return mDirectory;
  }

  private String getExtension(String fileName)
  {
    int pos = fileName.lastIndexOf(".");
    if (pos > 0)
      return fileName.substring(pos + 1);
    return "";
  }

  private String getMimeType(String extension)
  {
    // TODO: think about this. Possibly use a MAP loaded at init.
    if ("css".equalsIgnoreCase(extension))
      return "text/css";
    if ("js".equalsIgnoreCase(extension))
      return "application/javascript";
    if ("jpg".equalsIgnoreCase(extension))
      return "image/jpg";
    if ("png".equalsIgnoreCase(extension))
      return "image/png";
    if ("html".equalsIgnoreCase(extension))
      return "text/html";
    if ("txt".equalsIgnoreCase(extension))
      return "text/plain";
    if ("json".equalsIgnoreCase(extension))
      return "application/json";
    if ("xml".equalsIgnoreCase(extension))
      return "application/xml";

    // If unknown
    return "application/unknown";
  }

  private String mDirectory;
}
