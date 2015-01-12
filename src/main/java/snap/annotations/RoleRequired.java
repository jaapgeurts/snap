package snap.annotations;

public @interface RoleRequired
{
  public String role() default "";
}
