package snap.forms.annotations;

public @interface EnumMapping
{
  public String val() default "";

  public String label() default "";
}
