package snap.forms;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.forms.internal.FileField;
import snap.forms.internal.FormBase;
import snap.forms.internal.MultiCheckboxField;
import snap.http.RequestContext;

/**
 * Subclass this class to create a new form. You can add any field and annotate
 * it with the html input type you wish to use. Only fields that have the
 * annotation in forms.annotations will be used in the HTML.
 * 
 * It's best to use wrapper objects instead of native types
 * 
 * 
 * @author Jaap Geurts
 *
 */
public class Form
{

  final static Logger log = LoggerFactory.getLogger(Form.class);

  public Form()
  {
    mFieldList = new ArrayList<FormField>();
    mFieldMap = new HashMap<String, FormField>();

    initFields();

    mFormName = getClass().getCanonicalName();
    mFormName = mFormName.substring(mFormName.lastIndexOf('.') + 1,
        mFormName.length());
  }

  private void initFields()
  {

    Field[] classFields = getClass().getDeclaredFields();
    for (Field classField : classFields)
    {
      Annotation[] annotations = classField.getAnnotations();

      // TODO: Consider using classField.getAnnotationByType();
      // TODO: could cache the reflection field object in the Html Field
      // representation obj
      for (Annotation annotation : annotations)
      {
        FormField field = null;
        String fieldName = classField.getName();
        if (annotation instanceof snap.forms.annotations.TextField)
        {
          snap.forms.annotations.TextField tfa = (snap.forms.annotations.TextField)annotation;
          field = new snap.forms.internal.TextField(this, classField, tfa);
        }
        else if (annotation instanceof snap.forms.annotations.TextArea)
        {
          snap.forms.annotations.TextArea taa = (snap.forms.annotations.TextArea)annotation;
          field = new snap.forms.internal.TextArea(this, classField, taa);
        }
        else if (annotation instanceof snap.forms.annotations.CheckBoxField)
        {
          snap.forms.annotations.CheckBoxField cba = (snap.forms.annotations.CheckBoxField)annotation;
          field = new snap.forms.internal.CheckBoxField(this, classField, cba);
        }
        else if (annotation instanceof snap.forms.annotations.RadioField)
        {
          snap.forms.annotations.RadioField rfa = (snap.forms.annotations.RadioField)annotation;
          field = new snap.forms.internal.RadioField(this, classField, rfa);
        }
        else if (annotation instanceof snap.forms.annotations.MultiCheckboxField)
        {
          snap.forms.annotations.MultiCheckboxField msfa = (snap.forms.annotations.MultiCheckboxField)annotation;
          field = new snap.forms.internal.MultiCheckboxField(this, classField,
              msfa);
        }
        else if (annotation instanceof snap.forms.annotations.ListField)
        {
          snap.forms.annotations.ListField ddla = (snap.forms.annotations.ListField)annotation;
          field = new snap.forms.internal.ListField(this, classField, ddla);
        }
        else if (annotation instanceof snap.forms.annotations.SubmitField)
        {
          snap.forms.annotations.SubmitField sfa = (snap.forms.annotations.SubmitField)annotation;
          field = new snap.forms.internal.SubmitButton(this, classField, sfa);
        }
        else if (annotation instanceof snap.forms.annotations.HiddenField)
        {
          snap.forms.annotations.HiddenField hfa = (snap.forms.annotations.HiddenField)annotation;
          field = new snap.forms.internal.HiddenField(this, classField, hfa);
        }
        else if (annotation instanceof snap.forms.annotations.FileField)
        {
          snap.forms.annotations.FileField ffa = (snap.forms.annotations.FileField)annotation;
          field = new snap.forms.internal.FileField(this, classField, ffa);
        }
        else if (annotation instanceof snap.forms.annotations.PasswordField)
        {
          snap.forms.annotations.PasswordField pwfa = (snap.forms.annotations.PasswordField)annotation;
          field = new snap.forms.internal.PasswordField(this, classField, pwfa);
        }
        if (field != null)
        {
          mFieldList.add(field);
          mFieldMap.put(fieldName, field);
        }
      }
    }
  }

  /**
   * Populates this form with values contained in the request. If the field is a
   * select field, this method will check if the posted value matches one of the
   * values in the choice
   * 
   * @param context
   * @throws InvalidCsrtToken
   *           or MissingCsrfToken when the token is invalid or missing
   */
  public void assignFieldValues(RequestContext context)
  {
    if (context == null)
      return;

    // throws exception if token is not present
    doCheckCsrfToken(context);

    Map<String, String[]> params = context.getParamsPostGet();

    // for all fields find parameters in the request and assign

    // TODO: rewrite and let the subclasses handle assignment.

    for (Entry<String, FormField> entry : mFieldMap.entrySet())
    {
      // Make an exception for FileField since it doesn't take a string
      // but takes a Part class
      // TODO: consider changing this.
      if (entry.getValue() instanceof FileField)
      {
        FileField ff = (FileField)entry.getValue();
        try
        {
          Collection<Part> parts = context.getRequest().getParts();
          for (Part p : parts)
          {
            if (p.getName().equals(entry.getKey()))
              ff.setFieldValue(p);
          }
        }
        catch (IOException | ServletException e)
        {
          log.debug("Can't get multipart class", e);
        }

      }
      else
      {
        ((FormBase)entry.getValue()).setFieldValue(params.get(entry.getKey()));
      }
    }
  }

  public static boolean validateCsrfToken(RequestContext context)
  {
    // if the user is not logged in do nothing.
    if (context.getAuthenticatedUser() == null)
    {
      log.debug("User not logged in. Not checking Csrf token");
      return false;
    }

    String token = context.getParamPostGet("csrf_token");
    if (token == null)
      // attempt to get the token from the HTTP header X-CSRFToken
      token = context.getRequest().getHeader("X-CSRFToken");

    if (token == null)
      throw new MissingCsrfTokenException(
          "Token not found in parameters or CSRFToken header.");

    if (!token.equals(context.getCsrfToken()))
      throw new InvalidCsrfTokenException(
          "The submitted csrf token value did not match the expected value.");

    return true;
  }

  private boolean doCheckCsrfToken(RequestContext context)
  {

    try
    {
      return validateCsrfToken(context);
    }
    catch (MissingCsrfTokenException mcte)
    {
      String message = "Csrf Token not found for form: "
          + this.getClass().getCanonicalName()
          + ". Did you forget to include it with @csrf_token()";

      log.debug(message, mcte);
      throw mcte;
    }
    catch (InvalidCsrfTokenException icte)
    {
      String message = "Token value invalid";
      log.debug(message, icte);
      throw icte;
    }
  }

  /**
   * Renders the form as HTML tags. Also renders errors if any. Layout is
   * applied according to the type parameter.
   * 
   * @param type
   *          Specifies the way the form should be layed out.
   *          <ul>
   *          <li>null - Don't apply any formatting</li>
   *          <li>"table" - Format with table/tr/td tags</li>
   *          <li>"div" - Format with div tags</li>
   *          </ul>
   * @return A complete string of all the html form fields
   */
  public String render(String type)
  {
    String startTag = "";
    String endTag = "";
    String rowOpenTag = "";
    String rowCloseTag = "";

    if (type != null)
    {
      String t = type.trim().toLowerCase();
      if (t.equals("table"))
      {
        // set css class to the form name
        startTag = "<table class=\"form-table\">";
        endTag = "</table>";
        rowOpenTag = "<tr class=\"form-table-row\"><td class=\"form-table-cell\">";
        rowCloseTag = "</td></tr>";
      }
      else if (t.equals("div"))
      {
        startTag = "<div class=\"form-div\">";
        endTag = "</div>";
        rowOpenTag = "<div class=\"form-div-row\">";
        rowCloseTag = "</div>";
      }
    }
    // TODO: render a CSRF token
    StringBuilder builder = new StringBuilder();
    if (mFormError != null && !"".equals(mFormError))
    {
      builder.append("<p class=\"form-error\">");
      builder.append(getFormError());
      builder.append("</p>");
    }
    builder.append(startTag);
    for (FormField field : mFieldList)
    {
      builder.append(rowOpenTag);
      builder.append(field.render());
      builder.append(rowCloseTag);
      if (field.hasError())
      {
        builder.append("<p class=\"field-error\">");
        builder.append(field.getError());
        builder.append("</p>");
      }
    }
    builder.append(endTag);
    return builder.toString();
  }

  /**
   * Renders a specific field as HTML. Also renders errors if any
   * 
   * @param fieldName
   *          The field to be rendered
   * @return The HTML string
   */
  public String renderField(String fieldName)
  {
    FormField field = mFieldMap.get(fieldName);
    if (field instanceof MultiCheckboxField)
    {
      throw new RuntimeException(
          "Field render requested for MultiSelectField, but wrong method called. Call: renderField(String fieldName, Object value) instead.");
    }
    return field.render();
  }

  /**
   * Renders a specific field as HTML with a value. Also renders errors if any
   * 
   * @param fieldName
   *          The field to render
   * @param value
   *          The value to use for the field
   * @return The HTML string
   */
  public String renderField(String fieldName, Object value)
  {
    FormField field = mFieldMap.get(fieldName);
    if (field instanceof MultiCheckboxField)
    {
      MultiCheckboxField msf = (MultiCheckboxField)field;
      return msf.render(value.toString());
    }
    throw new RuntimeException(
        "Field render requested on normal field but method called for MultiSelectField. Call renderField(String fieldName) instead.");
  }

  /**
   * Returns a Field Subtype Object that represents the Annotated Form field.
   * 
   * @param fieldName
   *          The name of the field you want to get
   * @return The Form field
   */
  public FormField field(String fieldName)
  {
    return mFieldMap.get(fieldName);
  }

  /**
   * Validates the form. Runs the Hibernate Validator to check if the form
   * values are correct. You can override this method if you want to implement
   * extra or different logic.
   * 
   * @return True, when there are no errors, false if there are
   */
  public boolean isValid()
  {
    Validator validator = Validation.buildDefaultValidatorFactory()
        .getValidator();

    Set<ConstraintViolation<Form>> constraintViolations = validator
        .validate(this);

    for (ConstraintViolation<Form> cv : constraintViolations)
    {
      FormField field = mFieldMap.get(cv.getPropertyPath().toString());
      if (field != null)
        field.setError(cv.getMessage());
    }

    return constraintViolations.isEmpty();

  }

  /**
   * Returns true if there are error messages present in this form or in any of
   * the form fields
   * 
   * @return true or false
   */
  public boolean hasErrors()
  {
    if (mFormError != null)
      return true;

    for (FormField field : mFieldList)
      if (field.hasError())
        return true;

    return false;
  }

  /**
   * Sets the error of this form
   * 
   * @return
   */
  public String getFormError()
  {
    return mFormError;
  }

  /**
   * Sets the error string of this form
   * 
   * @param formError
   */
  public void setFormError(String formError)
  {
    mFormError = formError;
  }

  public void clearAllErrors()
  {
    mFormError = null;
    for (FormField field : mFieldList)
      field.clearError();
  }

  private List<FormField> mFieldList;
  private Map<String, FormField> mFieldMap;
  private String mFormError = null;
  private String mFormName;
}
