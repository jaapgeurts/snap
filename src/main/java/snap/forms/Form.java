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
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.Helpers;
import snap.SnapException;
import snap.forms.internal.FileField;
import snap.forms.internal.FormFieldBase;
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
    mFormErrors = new ArrayList<String>();

    initFields();

    mFormName = getClass().getCanonicalName();
    mFormName = mFormName.substring(mFormName.lastIndexOf('.') + 1, mFormName.length());
  }

  private void initFields()
  {

    Field[] classFields = getClass().getDeclaredFields();
    for (Field classField : classFields)
    {
      Annotation[] annotations = classField.getAnnotations();

      // Loop throw all the annotations on the class and create a field
      // according to its declared kind
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
          field = new snap.forms.internal.MultiCheckboxField(this, classField, msfa);
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
   *          The context of the current request from which the params will be
   *          read
   */
  public void assignFieldValues(RequestContext context)
  {
    if (context == null)
      return;

    Map<String, String[]> params = context.getParamsPostGet();

    // for all fields find parameters in the request and assign

    for (Entry<String, FormField> entry : mFieldMap.entrySet())
    {
      // Make an exception for FileField since it doesn't take a string
      // but takes a Part class
      // We need to get the parts here because it is taken from the context.
      // Fields don't have context.
      // the filefield is special ass setfield(part) will add it to the set
      // if the field is a Set<Part>
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
        ((FormFieldBase)entry.getValue()).setFieldValue(params.get(entry.getKey()));
      }
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
        startTag = "<table class='form-table'>";
        endTag = "</table>";
        rowOpenTag = "<tr class='form-table-row'><td class='form-table-cell'>";
        rowCloseTag = "</td></tr>";
      }
      else if (t.equals("div"))
      {
        startTag = "<div class='form-div'>";
        endTag = "</div>";
        rowOpenTag = "<div class='form-div-row'>";
        rowCloseTag = "</div>";
      }
    }

    // render all fields
    StringBuilder builder = new StringBuilder();
    builder.append(renderFormErrors());
    builder.append(startTag);
    for (FormField field : mFieldList)
    {
      builder.append(rowOpenTag);
      builder.append(renderLabel(field, new HashMap<String, Object>()));
      builder.append(field.render());
      builder.append(rowCloseTag);
    }
    builder.append(endTag);
    return builder.toString();
  }

  /**
   * Renders a specific field as HTML with a value. Also renders errors if any
   * 
   * @param fieldName
   *          The field to render
   * @param attributes
   *          Extra attributes for the field
   * @return The HTML string
   */
  public String renderField(String fieldName, Map<String, Object> attributes)
  {
    FormField field = mFieldMap.get(fieldName);

    if (field == null)
      throw new SnapException("Rendering of non-existing field " + fieldName);

    Map<String, String> attribs = new HashMap<>();
    attributes.entrySet().stream().forEach(e -> attribs.put(e.getKey(), e.getValue().toString()));

    // Merge in the attributes from the html template
    // the attributes from code take precedence
    // except for class attributes, they get added
    String classHtml = attribs.get("class");
    String classCode = field.getAttribute("class");
    String classMerged = null;

    if (classHtml != null && classHtml.length() > 0)
      classMerged = classHtml;
    if (classCode != null && classCode.length() > 0)
    {
      if (classMerged != null && classMerged.length() > 0)
        classMerged += " ";
      classMerged += classCode;
    }

    attribs.putAll(field.getAttributes());
    if (classMerged != null && classMerged.length() > 0)
      attribs.put("class", classMerged);

    return field.render(attribs);

  }

  /**
   * Renders a label for a specific field
   * 
   * @param fieldName
   *          The field to render the label for
   * @param attributes
   *          Extra attributes to add to the label
   * @return the HTML string
   */
  public String renderLabel(String fieldName, Map<String, Object> attributes)
  {
    FormField field = mFieldMap.get(fieldName);
    return renderLabel(field, attributes);
  }

  public String renderLabel(FormField field, Map<String, Object> attributes)
  {
    if (field == null)
      throw new SnapException("Rendering of label for non-existing field");

    String htmlId = field.getHtmlId();
    String label = field.getLabel();

    if (label == null)
      return "";

    // Convert <string,object> map to <string,string> map
    Map<String, String> attribs = new HashMap<>();
    attributes.entrySet().stream().forEach(e -> attribs.put(e.getKey(), e.getValue().toString()));

    return String.format("<label for='%1$s' %3$s>%2$s</label>\n", htmlId, label,
        Helpers.attrToString(attribs));
  }

  /**
   * If the field has error information set it will return a rendered error html
   * node. It will return the error enclosed in a SPAN element.
   * 
   * @param fieldName
   *          The field name
   * @param attributes
   *          Additional attributes for the SPAN element
   * @return the rendered string
   */
  public String renderFieldError(String fieldName, Map<String, Object> attributes)
  {
    FormField field = mFieldMap.get(fieldName);

    if (field == null)
      throw new SnapException("Rendering of non-existing field " + fieldName);

    if (!field.hasError())
      return "";

    String attribs = attributes.entrySet().stream()
        .map(e -> e.getKey() + "='" + e.getValue().toString() + "'").collect(Collectors.joining(" "));

    return String.format("<span %1$s>%2$s</span>", attribs, field.getError());

  }

  public String renderFormErrors()
  {
    if (!hasFormErrors())
      return "";

    if (mFormErrors.size() == 1)
      return mFormErrors.get(0);

    StringBuilder builder = new StringBuilder();
    builder.append("<ul>");
    for (String error : mFormErrors)
      builder.append("<li>").append(error).append("</li>");
    builder.append("</ul>");
    return builder.toString();
  }

  /**
   * Returns a Field Subtype Object that represents the Annotated Form field.
   * 
   * @param fieldName
   *          The name of the field you want to get
   * @return The Form field or null if not found
   */
  public FormField getField(String fieldName)
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
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    Set<ConstraintViolation<Form>> constraintViolations = validator.validate(this);

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
    if (mFormErrors != null)
      return true;

    for (FormField field : mFieldList)
      if (field.hasError())
        return true;

    return false;
  }

  /**
   * Returns whether this form has form specific errors
   * 
   * @return true or false
   */
  public boolean hasFormErrors()
  {
    return mFormErrors.size() > 0;
  }

  /**
   * Returns true if the specified field has an error
   * 
   * @param fieldName
   *          the field to query
   * @return true of false
   */
  public boolean hasError(String fieldName)
  {
    FormField field = mFieldMap.get(fieldName);

    if (field == null)
      throw new SnapException("Field: " + fieldName + " does not exist in Form: " + mFormName);

    return field.hasError();
  }

  /**
   * Returns the errors of this form
   * 
   * @return The error string.
   */
  public List<String> getErrors()
  {
    return mFormErrors;
  }

  /**
   * Sets the error string of this form
   * 
   * @param formError
   *          The error string
   */
  public void addError(String formError)
  {
    mFormErrors.add(formError);
  }

  /**
   * Set an error on a field. This is a shortcut to calling
   * 
   * <pre>
   * getField(fieldName).setError(errorText)
   * </pre>
   * 
   * @param fieldName
   *          The name of the field
   * @param errorText
   *          The error text
   */
  public void setFieldError(String fieldName, String errorText)
  {
    FormField f = getField(fieldName);
    if (f != null)
      f.setError(errorText);
  }

  /**
   * Clear the error on a field. This is a shortcut to calling
   * 
   * <pre>
   * getField(fieldName).clearError()
   * </pre>
   * 
   * @param fieldName
   *          The name of the field
   */
  public void clearFieldError(String fieldName)
  {
    FormField f = getField(fieldName);
    if (f != null)
      f.clearError();
  }

  /**
   * Clear all form and field errors
   */
  public void clearAllErrors()
  {
    mFormErrors.clear();
    for (FormField field : mFieldList)
      field.clearError();
  }

  private List<FormField> mFieldList;
  private List<String> mFormErrors;
  private Map<String, FormField> mFieldMap;
  private String mFormName;

}
