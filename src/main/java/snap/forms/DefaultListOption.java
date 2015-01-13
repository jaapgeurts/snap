package snap.forms;

public class DefaultListOption implements ListOption
{

  private String mValue;
  private String mText;

  public DefaultListOption(String value, String text)
  {
    mValue = value;
    mText = text;
  }

  @Override
  public String getValue()
  {
    return mValue;
  }

  @Override
  public String getText()
  {
    return mText;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    if (this == obj)
      return true;

    if (!(obj instanceof DefaultListOption))
      return false;

    DefaultListOption dlo = (DefaultListOption)obj;

    return mText.equals(dlo.mText) && mValue.equals(dlo.mValue);

  }

  @Override
  public int hashCode()
  {
    int result = 37 * mText.hashCode();
    result = 37 * mValue.hashCode() + result;
    return result;
  }

}
