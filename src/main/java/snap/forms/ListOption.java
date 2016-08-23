package snap.forms;

/**
 * Interface for use with RadioFields, ListBoxes, Combobox.
 *
 * @author Jaap Geurts
 *
 */
public interface ListOption
{
  /**
   * Should return the value that is assigned to the 'value' attribute of the
   * html tag. This value is posted back to the server and used by your code.
   *
   * @return
   */
  String getValue();

  /**
   * Should return the text that is presented to the user.
   *
   * @return
   */
  String getText();

  /**
   * You can optionally return an object that represents this ListOption
   *
   * @return
   */
  Object getOption();

}
