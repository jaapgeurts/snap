package snap.forms;

/**
 * This List option allows you to use any type for a ListField. You will have to
 * pass the ListField value and text and pass an object, The object it later used
 * to assign the value back to the field on postback.
 * If you want to use Enums you should use EnumListOption, if you want to use primitive values such as
 * Integer, Long, String use PrimitiveListOption.
 *
 * @author Jaap Geurts
 *
 * @param <T>
 *          The type to use for this list option
 */
public class GenericListOption<T> implements ListOption
{

  private String mValue;
  private String mText;
  private T mOption;

  public GenericListOption(String value, String text, T option)
  {
    mValue = value;
    mText = text;
    mOption = option;
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
  public T getOption()
  {
    return mOption;
  }

  @Override
  public boolean equals(Object obj)
  {
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
