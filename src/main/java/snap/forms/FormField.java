package snap.forms;

import java.util.Map;

public interface FormField
{

  public String render();

  public String getError();
  public void setError(String errorText);
  public void clearError();
  public boolean hasError();
  public void setVisible(boolean visible);
  public boolean isVisible();
  public void setHtmlId(String htmlId);
  public String getHtmlId();
  public void setLabel(String label);
  public String getLabel();
  public String getLabel(String which);
  public void setCssClass(String cssClass);
  public String getCssClass();
  
  public void addAttribute(String attrib, String value);
  public void removeAttribute(String attrib);
  public String getAttribute(String attrib);
  
  /**
   * Merges the in attributes with the field attributes. The in attributes take
   * priority over the field attributes
   * 
   * @param in
   *          Map of key value pairs to merge.
   * @param overwrite
   *          Overwrite the values of in into the object attributes. Values for
   *          the class attribute are concatenated by default
   */
  public void mergeAttributes(Map<String, Object> in, boolean overwrite);



}
