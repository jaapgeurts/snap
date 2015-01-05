package com.proficiosoftware.snap.forms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a dropdown list otherwise known as a &lt;select&gt; The list gets its
 * options from the list specified in optionList optionList could be a list of
 * object List<Object> or field of type List<ListOption>. In case of Object
 * toString() will be used for both value and text. In case of ListOption you
 * can set separate values and text
 * 
 * @author Jaap Geurts
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DropDownList
{
  public String id() default "";

  public String label() default "";

  public String optionList() default "";
}
