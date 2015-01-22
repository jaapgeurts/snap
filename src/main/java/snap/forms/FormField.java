package snap.forms;

public interface FormField
{

  public String render();

  public String getError();
  public void setError(String errorText);
  public void clearError();
  public boolean hasError();
  public void setVisible(boolean visible);
  public boolean isVisible();

}
