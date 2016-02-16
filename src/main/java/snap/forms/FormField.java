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
  public void setLabel(String label);
  public String getLabel();
  public void setCssClass(String cssClass);
  public String getCssClass();
  
  public void addAttribute(String attrib, String value);
  public String getAttribute(String attrib);
  
  public void mergeAttributes(Map<String, Object> in, boolean overwrite);

}
