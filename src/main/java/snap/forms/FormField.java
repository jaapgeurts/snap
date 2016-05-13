package snap.forms;

import java.util.Map;

public interface FormField
{

  /**
   * Renders the field
   * 
   * @return HTML for the field
   */
  public String render();

  /**
   * Renders the field with the specified attributes
   * 
   * @param attributes HTML tag attributes
   * @return HTML for the field
   */
  public String render(Map<String, String> attributes);

  /**
   * Get this fields error string
   * 
   * @return The error string
   */
  public String getError();

  /**
   * Set this fields error string
   * 
   * @param errorText The error to display
   */
  public void setError(String errorText);

  /**
   * Clear this fields error string
   */
  public void clearError();

  /**
   * returns true if an error string has been set. Clear with clearError()
   * 
   * @return true if an error has been set.
   */
  public boolean hasError();

  /**
   * Prevent this field from rendering
   * 
   * @param visible true or false
   */
  public void setVisible(boolean visible);

  /**
   * Returns true if this field will be rendered
   * 
   * @return true if this field will be rendered
   */
  public boolean isVisible();

  /**
   * Set the HTML Id for this field. You would normally set this trough its
   * corresponding annotation
   * 
   * @param htmlId the  ID to use
   */
  public void setHtmlId(String htmlId);

  /**
   * Get the HTML id for this field
   * 
   * @return The HTML id
   */
  public String getHtmlId();

  /**
   * If this field has multiple values (such as a listbox or a radio list then
   * this field will the ID for the value specified by which
   * 
   * @param which
   *          the value for which to lookup the HTML id
   * @return the HTML id
   */
  public String getHtmlId(String which);

  /**
   * Sets the label for this field. You would normally set this through its
   * corresponding annotation
   * 
   * @param label the label text
   */
  public void setLabel(String label);

  /**
   * Get the text for the label to use
   * 
   * @return the text for the label
   */
  public String getLabel();

  /**
   * If this field has multiple values (such as a listbox or a radio list then
   * this field returns the label text for this particular value
   * 
   * @param which the value of the label 
   * @return the text for the label for the value indicated by which
   */
  public String getLabel(String which);

  /**
   * Returns all possible field values for this field
   * 
   * @return a list of strings
   */
  public String[] getOptions();

  public void addAttribute(String attrib, String value);

  public void removeAttribute(String attrib);

  public String getAttribute(String attrib);

  public Map<String, String> getAttributes();

}
