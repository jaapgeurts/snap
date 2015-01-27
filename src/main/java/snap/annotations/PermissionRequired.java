package snap.annotations;

public @interface PermissionRequired
{
  public String permission() default "";
}
