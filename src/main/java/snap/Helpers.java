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

}
