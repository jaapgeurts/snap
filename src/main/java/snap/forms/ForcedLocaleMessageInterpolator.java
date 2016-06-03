package snap.forms;

import java.util.Locale;

import javax.validation.MessageInterpolator;

public class ForcedLocaleMessageInterpolator implements MessageInterpolator
{

  private MessageInterpolator interpolator;
  private Locale locale;

  public ForcedLocaleMessageInterpolator(MessageInterpolator interpolator, Locale locale)
  {
    this.interpolator = interpolator;
    this.locale = locale;
  }

  @Override
  public String interpolate(String messageTemplate, Context context)
  {
    return interpolator.interpolate(messageTemplate, context, locale);
  }

  @Override
  public String interpolate(String messageTemplate, Context context, Locale locale)
  {
    return interpolator.interpolate(messageTemplate, context, locale);
  }

}
