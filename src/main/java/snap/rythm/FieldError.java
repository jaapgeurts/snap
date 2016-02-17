package snap.rythm;

import java.util.Map;

import org.rythmengine.template.JavaTagBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.SnapException;

public class FieldError extends JavaTagBase
{
  final Logger log = LoggerFactory.getLogger(FieldError.class);

  public FieldError()
  {
  }

  @Override
  public String __getName()
  {
    return "fielderror";
  }

  @Override
  protected void call(__ParameterList params, __Body body)
  {
    Object o;
    String name;
    snap.forms.Form form;

    // test if params were passed by name
    Map<String, Object> paramMap = params.asMap();
    if (paramMap.size() > 0)
    {
      o = paramMap.get("form");
      if (o == null)
        throw new SnapException(
            "@field missing 'form' argument. You must specify the form that contains the field");
      form = (snap.forms.Form)o;
      paramMap.remove("form");

      o = paramMap.get("field");
      if (o == null)
        throw new SnapException(
            "@field missing 'field' argument. You must specify the field in the form");
      name = o.toString();
      paramMap.remove("field");

    }
    else
    // params were passed by position
    {

      o = params.getDefault();

      if (o == null || !(o instanceof snap.forms.Form))
        throw new SnapException(
            "@field missing form argument. Specify as first parameter or use the parameter name 'form'");
      form = (snap.forms.Form)o;

      if (params.size() > 2)
        log.warn("@field in form: '" + form.toString()
            + "', positional parameter two and after ignored.");

      // second parameter (this is the field name)
      o = params.getByPosition(1);
      if (o == null)
        throw new SnapException(
            "@field missing name argument. Specify as second parameter or use the parameter name 'name'");
      name = o.toString();

    }

    p(form.renderFieldError(name, paramMap));
  }
}
