package snap.rythm;

import org.rythmengine.extension.Transformer;

@Transformer("x")
public class SpaceToNonBreakingSpaceTransformer
{

  public static String s2nbsp(String text)
  {
    if (text == null)
      return "";
    return text.replace(" ", "&nbsp;");
  }
}
