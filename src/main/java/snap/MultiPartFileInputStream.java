package snap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MultiPartFileInputStream extends InputStream
{
  private static final String NEWLINE = "\r\n";
  private static final String CONTENT_TYPE = "Content-Type: ";
  private static final String CONTENT_RANGE = "Content-Range: bytes ";
  private static final int BLOCK = 1;
  private static final int DATA = 2;

  private MultiPartFile multiPartFile;
  private RandomAccessFile fileHandle;
  private byte[] multiPartBlock;
  List<Pair<Long, Long>> ranges;

  private int rangeIndex = 0;
  private long byteOffset = 0;
  private int dataOrBlock = BLOCK;

  public MultiPartFileInputStream(MultiPartFile multiPartFile) throws FileNotFoundException
  {
    this.multiPartFile = multiPartFile;
    ranges = multiPartFile.getRanges();
    fileHandle = new RandomAccessFile(multiPartFile.getFile(), "r");
    createNextBlock();
  }

  @Override
  public void reset()
  {
    rangeIndex = 0;
    byteOffset = 0l;
    dataOrBlock = BLOCK;
  }

  @Override

  public long skip(long n)
  {
    throw new UnsupportedOperationException("Not yet implemented");
    // return 0l;
  }

  @Override
  public boolean markSupported()
  {
    return false;
  }

  @Override
  public int available()
  {
    if (rangeIndex > ranges.size())
      return 0;

    if (rangeIndex == ranges.size())
      return multiPartBlock.length - (int)byteOffset;

    return 1;
  }

  @Override
  public int read() throws IOException
  {
    int result;
    if (dataOrBlock == BLOCK)
    {
      result = (int)multiPartBlock[(int)byteOffset];
    }
    else
    {
      // DATA
      if (!isPointerValid())
        return -1;

      Pair<Long, Long> pair = ranges.get(rangeIndex);

      fileHandle.seek(pair.getFirst() + byteOffset);
      result = fileHandle.read();

    }
    advancePointer();
    return result;

  }

  @Override
  public void close() throws IOException
  {
    fileHandle.close();
    super.close();
  }

  /**
   * Advance the byte pointer. The current rangeindex includes the block. when
   * the data is finished advance the pointer to the next block when the block
   * is finished switch to the data
   */
  private void advancePointer()
  {
    byteOffset++;
    if (dataOrBlock == BLOCK)
    {
      if (byteOffset >= multiPartBlock.length)
      {
        dataOrBlock = DATA;
        byteOffset = 0l;
      }
    }
    else
    { // data
      if (byteOffset > ranges.get(rangeIndex).getSecond())
      {
        dataOrBlock = BLOCK;
        byteOffset = 0l;
        rangeIndex++;
        createNextBlock();
      }
    }
  }

  /**
   * Checks if the data pointer is valid
   * 
   * @return
   * @throws IOException
   */
  private boolean isPointerValid() throws IOException
  {
    if (rangeIndex >= ranges.size())
      return false;

    if (byteOffset > ranges.get(rangeIndex).getSecond())
      return false;

    if (byteOffset >= fileHandle.length())
      return false;

    return true;

  }

  private void createNextBlock()
  {
    String multiPartString = "";
    // No more valid blocks.
    if (rangeIndex > ranges.size())
    {
      multiPartBlock = new byte[0];
    }
    else

    if (rangeIndex == ranges.size())
    {
      // reached the last block. Set the closure.
      multiPartString = "\r\n--" + multiPartFile.getBoundaryToken() + "--";
      multiPartBlock = multiPartString.getBytes(StandardCharsets.UTF_8);
    }
    else
    {
      Pair<Long, Long> pair = ranges.get(rangeIndex);
      multiPartString = NEWLINE + "--" + multiPartFile.getBoundaryToken() + NEWLINE;
      multiPartString += CONTENT_TYPE + multiPartFile.getContentType() + NEWLINE;
      multiPartString += CONTENT_RANGE + pair.getFirst().toString() + "-" + pair.getSecond().toString() + "/"
          + multiPartFile.getFile().length() + NEWLINE + NEWLINE;
    }
    multiPartBlock = multiPartString.getBytes(StandardCharsets.UTF_8);

  }

}
