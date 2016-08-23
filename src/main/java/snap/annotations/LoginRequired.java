package snap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to make sure a user is logged in. A user is logged in
 * when
 *
 * <pre>
 * context.getAuthenticatedUser() != null
 * </pre>
 *
 * This annotation can be placed on a controller method or on a controller
 * class. If put on a controller class all controller methods in that class
 * require an authenticated user. When put on a class you can mark a method
 * with @IgnoreLoginRequired to make an exception
 *
 * @author Jaap Geurts
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface LoginRequired
{

}
