package snap;

import java.util.Map;
import java.util.stream.Collectors;

public class Helpers
{

  public static String attrToString(Map<String, String> attribs)
  {
    return attribs.entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'")
        .collect(Collectors.joining(" "));
  }

  public static String escapeHtml(String str)
  {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < str.length(); i++)
    {
      char c = str.charAt(i);
      switch(c)
      {
        case '\"':
          builder.append("&quot;");
          break;
        case '\'':
          builder.append("&#39;");
          break;
        case '<':
          builder.append("&lt;");
          break;
        case '>':
          builder.append("&gt;");
          break;
        case '&':
          builder.append("&amp;");
          break;
        default:
          builder.append(c);
      }
    }
    return builder.toString();
  }

}
