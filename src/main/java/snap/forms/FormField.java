package snap.forms;

import java.util.Map;

public interface FormField
{

  public String render();
  public String render(Map<String, String> attributes);

  public String getError();
  public void setError(String errorText);
  public void clearError();
  public boolean hasError();
  public void setVisible(boolean visible);
  public boolean isVisible();
  public void setHtmlId(String htmlId);
  public String getHtmlId();
  public String getHtmlId(String which);
  public void setLabel(String label);
  public String getLabel();
  public String getLabel(String which);
  
  public void addAttribute(String attrib, String value);
  public void removeAttribute(String attrib);
  public String getAttribute(String attrib);
  public Map<String, String> getAttributes();

}
