package snap.forms;

public class EnumListOption<T extends Enum<T>> implements ListOption
{

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
      return null;
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
