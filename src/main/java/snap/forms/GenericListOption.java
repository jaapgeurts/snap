package snap.forms;

/**
 * This List option uses toString to get the String representation of the value.
 * If you want to use Enums you should use EnumListOption
 * 
 * @author Jaap Geurts
 *
 * @param <T>
 *          The type to use for this list option
 */
public class GenericListOption<T> implements ListOption
{

  private T mValue;
  private String mText;

  public GenericListOption(T value, String text)
  {
    mValue = value;
    mText = text;
  }

  @Override
  public String getValue()
  {
    return mValue.toString();
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

    if (!(obj instanceof GenericListOption))
      return false;

    GenericListOption<?> dlo = (GenericListOption<?>)obj;

    return mText.equals(dlo.mText) && mValue.equals(dlo.mValue);

  }

  @Override
  public int hashCode()
  {
    int result = 37 * mText.hashCode();
    result = 37 * mValue.hashCode() + result;
    return result;
  }

  @Override
  public String toString()
  {
    return "GenericListOption: { text: " + mText + ", value: " + mValue.toString() + " }";
  }

}
