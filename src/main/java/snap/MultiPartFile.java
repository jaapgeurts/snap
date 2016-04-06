package snap;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class MultiPartFile
{

  private static final int BOUNDARY_INOUT_LEN = 2; // "--"
  private static final int NEWLINE_LEN = 1;
  private static final int CONTENT_TYPE_LEN = 14; // "Content-Type: "
  private static final int CONTENT_RANGE_LEN = 21; // "Content-Range: bytes "

  private List<Pair<Long, Long>> ranges;
  private File file;
  private String boundaryToken;
  private String contentType;

  private long contentLength;

  public MultiPartFile(File file, List<Pair<Long, Long>> ranges,
      String contentType)
  {
    this.ranges = ranges;
    this.file = file;
    this.boundaryToken = UUID.randomUUID().toString();
    this.contentType = contentType;

    final int FILE_LEN = Long.toString(file.length()).length();
    contentLength = 0;
    for (Pair<Long, Long> range : ranges)
    {
      contentLength += BOUNDARY_INOUT_LEN + boundaryToken.length() + NEWLINE_LEN
          + CONTENT_TYPE_LEN + contentType.length() + NEWLINE_LEN
          + CONTENT_RANGE_LEN + range.getFirst().toString().length() + 1
          + range.getSecond().toString().length() + 1 + FILE_LEN + NEWLINE_LEN
          + NEWLINE_LEN;
      contentLength += range.getSecond() - range.getFirst();
    }
    contentLength += NEWLINE_LEN + BOUNDARY_INOUT_LEN + boundaryToken.length()
        + BOUNDARY_INOUT_LEN;

  }

  public long getContentLength()
  {
    return contentLength;
  }

  public List<Pair<Long, Long>> getRanges()
  {
    return ranges;
  }

  public File getFile()
  {
    return file;
  }

  public String getBoundaryToken()
  {
    return boundaryToken;
  }

  public String getContentType()
  {
    return contentType;
  }

}
