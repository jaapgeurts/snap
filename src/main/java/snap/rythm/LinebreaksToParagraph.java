package snap.rythm;

import java.util.Map;

import org.rythmengine.template.JavaTagBase;

import snap.SnapException;

public class LinebreaksToParagraph extends JavaTagBase
{

  @Override
  public String __getName()
  {
    return "nl2p";
  }

  @Override
  protected void call(__ParameterList params, __Body body)
  {
    Map<String, Object> paramMap = params.asMap();
    String s = "";
    String attribs = "";

    if (paramMap.size() > 0) // passed by name
    {
      if (!paramMap.containsKey("text"))
        throw new SnapException("@nl2p() missing 'text' argument");
      // get the text value out and remove the key text.
      Object o = paramMap.get("text");
      s = (String)o.toString();

      paramMap.remove("text");
      // key text removed, now add any other parameters as html attribs

      if (paramMap.size() > 0)
      {
        for (Map.Entry<String, Object> e : paramMap.entrySet())
        {
          attribs += " " + e.getKey() + "='" + e.getValue().toString() + "'";
        }
      }
    }
    else
    {
      // check if any were passed by position
      Object o = params.getDefault();
      if (o != null)
        s = (String)o.toString();
    }
    p("<p" + attribs + ">" + s.trim().replaceAll("[\\r|\\n]+", "</p><p" + attribs + ">") + "</p>");
  }

}
