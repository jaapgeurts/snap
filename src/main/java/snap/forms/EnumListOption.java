package snap.forms;

public class EnumListOption<T extends Enum<T>> implements ListOption
{

  public static final String NULL_ID = "$NULL$";
  private Enum<T> mEnumObj;
  private String mLabel;

  public EnumListOption(Enum<T> value, String label)
  {
    mEnumObj = value;
    mLabel = label;
  }

  @Override
  public String getValue()
  {
    if (mEnumObj == null)
      return NULL_ID;
    else
      return mEnumObj.name();
  }

  @Override
  public String getText()
  {
    return mLabel;
  }

  @Override
  public Enum<T> getOption()
  {
    return mEnumObj;
  }

}
