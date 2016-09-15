package snap.forms;

import java.util.Map;

public interface FormField
{

  /**
   * Renders the field
   *
   * @return HTML for the field
   */
  String render();

  /**
   * Renders the field with the specified attributes
   *
   * @param attributes
   *          HTML tag attributes
   * @return HTML for the field
   */
  String render(Map<String, String> attributes);

  /**
   * Get this fields error string
   *
   * @return The error string
   */
  String getError();

  /**
   * Set this fields error string
   *
   * @param errorText
   *          The error to display
   */
  void setError(String errorText);

  /**
   * Clear this fields error string
   */
  void clearError();

  /**
   * returns true if an error string has been set. Clear with clearError()
   *
   * @return true if an error has been set.
   */
  boolean hasError();

  /**
   * Prevent this field from rendering
   *
   * @param visible
   *          true or false
   */
  void setVisible(boolean visible);

  /**
   * Returns true if this field will be rendered
   *
   * @return true if this field will be rendered
   */
  boolean isVisible();

  /**
   * Returns true if this is a required field. (This is marked by settings the
   * javax validation annotation: @NotNull
   *
   * @return true or false
   */
  boolean isRequired();

  /**
   * Set the HTML Id for this field. You would normally set this trough its
   * corresponding annotation
   *
   * @param htmlId
   *          the ID to use
   */
  void setHtmlId(String htmlId);

  /**
   * Get the HTML id for this field
   *
   * @return The HTML id
   */
  String getHtmlId();

  /**
   * If this field has multiple values (such as a listbox or a radio list then
   * this field will the ID for the value specified by which
   *
   * @param which
   *          the value for which to lookup the HTML id
   * @return the HTML id
   */
  String getHtmlId(String which);

  /**
   * Sets the label for this field. You would normally set this through its
   * corresponding annotation
   *
   * @param label
   *          the label text
   */
  void setLabel(String label);

  /**
   * Get the text for the label to use
   *
   * @return the text for the label
   */
  String getLabel();

  /**
   * If this field has multiple values (such as a listbox or a radio list then
   * this field returns the label text for this particular value
   *
   * @param which
   *          the value of the label
   * @return the text for the label for the value indicated by which
   */
  String getLabel(String which);

  /**
   * Returns all possible field values for this field
   *
   * @return a list of strings
   */
  String[] getOptions();

  /**
   * Add an attribute to add to the html tag.
   *
   * @param attrib
   *          the attribute to add
   * @param value
   *          the value to assign
   */
  void addAttribute(String attrib, String value);

  /**
   * Remove the attribute from the html tag. Only attributes added with
   * addAttribute() can be removed
   *
   * @param attrib
   *          The attribute to remove
   */
  void removeAttribute(String attrib);

  /**
   * Gets the attribute value with the specified name. Only values are returned
   * for attributes added with addAttribute()
   *
   * @param attrib
   *          The attribute to get the value for.
   * @return The value
   */
  String getAttribute(String attrib);

  /**
   * Gets a map of all the attributes. Only returns the attributes that were
   * added with addAttributes()
   *
   * @return the map with the attributes and values
   */
  Map<String, String> getAttributes();

}
