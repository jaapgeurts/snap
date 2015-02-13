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
    return mEnumObj.name();
  }

  @Override
  public String getText()
  {
    return mLabel;
  }

}
