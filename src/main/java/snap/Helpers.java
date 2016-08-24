package snap;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.stream.Collectors;

public class Helpers
{

  public static String attrToString(Map<String, String> attribs)
  {
    return attribs.entrySet().stream().map(Helpers::attribToString).collect(Collectors.joining(" "));
  }

  private static String attribToString(Map.Entry<String, String> e)
  {
    String key = e.getKey();
    String val = e.getValue();
    if (val.isEmpty())
      return key;

    return key + "='" + escapeHtml(val) + "'";
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

  public static SimpleImmutableEntry<String, String> splitQueryParam(String param)
  {
    String[] keyVal = param.split("=");
    if (keyVal.length == 1)
      return new SimpleImmutableEntry<String, String>(keyVal[1], null);
    if (keyVal.length > 1)
      return new SimpleImmutableEntry<String, String>(keyVal[0], keyVal[1]);
    throw new IllegalArgumentException("The param doesn't contain any value");
  }

}
