package snap.forms;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.Helpers;
import snap.Settings;
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
public abstract class Form
{

  final static Logger log = LoggerFactory.getLogger(Form.class);
  //
  // public static <T extends Form> T create(Class<T> clazz, RequestContext
  // context)
  // throws InstantiationException, IllegalAccessException
  // {
  //
  // T form = clazz.newInstance();
  // form.init(context);
  // form.onCreate();
  // return form;
  // }

  public Form(RequestContext context)
  {
    if (context == null)
      throw new IllegalArgumentException("Context parameter can't be null");

    mContext = context;

    mFieldList = new ArrayList<FormField>();
    mFieldMap = new HashMap<String, FormField>();
    mFormErrors = new ArrayList<String>();

    Class<?> clazz = getClass();
    mFormName = clazz.getCanonicalName();
    mFormName = mFormName.substring(mFormName.lastIndexOf('.') + 1, mFormName.length());

    initFields("", clazz);

    mLocale = mContext.getLocale();

    try
    {

      if (mLocale == null)
        mResourceBundle = ResourceBundle.getBundle(Settings.get("snap.i18n.resourcebundle.name", "messages"));
      else
        mResourceBundle = ResourceBundle.getBundle(Settings.get("snap.i18n.resourcebundle.name", "messages"),
                                                   mLocale);

    }
    catch (MissingResourceException mre)
    {
      if (mLocale != null)
      {
        log.warn("Locale resource bundle: " + Settings.get("snap.i18n.resourcebundle.name", "messages")
            + " can't be loaded. No translation is available for language " + mLocale.getLanguage());
        mLocale = null;
      }
      // else ignore because no language was set
    }
  }

  /**
   * Populates this form with values contained in the request.
   */
  public void assignFieldValues()
  {

    mIsAssigned = true;

    Map<String, String[]> params = mContext.getParamsPostGet();

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
        String contentType = mContext.getRequest().getContentType();
        if (contentType == null || "multipart/form-data".equals(contentType.toLowerCase()))
          continue;

        FileField ff = (FileField)entry.getValue();
        try
        {
          Collection<Part> parts = mContext.getRequest().getParts();
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
        if (params.containsKey(entry.getKey()))
          ((FormFieldBase)entry.getValue()).setFieldValue(params.get(entry.getKey()));
      }
    }
  }

  /**
   * Called when an error occured when assigning a value to a field. Most
   * usually these are type conversion errors
   *
   * @param fieldName
   *          The name of the field where the error happened
   * @param submittedValue
   *          Value The valued that was submitted for this field
   * @param exception
   *          the exception that occurred
   */
  public void onFieldAssignmentError(String fieldName, String submittedValue, Exception exception)
  {
    log.debug("Field assignment error for field: " + fieldName, exception);
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

  /**
   * Renders a label for a specific field
   *
   * @param field
   *          The field to render the label for
   * @param attributes
   *          Extra attributes to add to the label
   * @return the HTML string
   */
  public String renderLabel(FormField field, Map<String, Object> attributes)
  {
    if (field == null)
      throw new SnapException("Rendering of label for non-existing field");

    String htmlId = field.getHtmlId();
    String label = field.getLabel();

    if (label == null)
      return "";

    label = parseAnnotationString(label);

    // Convert <string,object> map to <string,string> map
    Map<String, String> attribs = new HashMap<>();
    attributes.entrySet().stream().forEach(e -> attribs.put(e.getKey(), e.getValue().toString()));

    // check if the fields are required and insert a star

    String required = "";
    if (field.isRequired())
      required = " <span class='required'>*</span>";

    return String.format("<label for='%1$s' %3$s>%2$s%4$s</label>\n", htmlId, label,
                         Helpers.attrToString(attribs),required);
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
    FormField field = getField(fieldName);

    if (!field.hasError())
      return "";

    String attribs = attributes.entrySet().stream()
        .map(e -> e.getKey() + "='" + e.getValue().toString() + "'").collect(Collectors.joining(" "));

    return String.format("<span %1$s>%2$s</span>", attribs, field.getError());

  }

  /**
   * If the form has errors defined this will return a HTML string with the
   * errors formatted in the UL tag.
   *
   * @return The list as a HTML string.
   */
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
    FormField field = mFieldMap.get(fieldName);
    if (field == null)
      throw new SnapException("Request access to non-existing field: " + fieldName);
    return field;
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

    if (!mIsAssigned)
      log.warn("Form " + mFormName
          + " is being validated but fields have not been assigned yet. Did you forget to call assignFieldValues();");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator;
    if (mLocale != null)
      validator = validatorFactory.usingContext()
          .messageInterpolator(
                               new ForcedLocaleMessageInterpolator(validatorFactory.getMessageInterpolator(),
                                   mLocale))
          .getValidator();
    else
      validator = validatorFactory.getValidator();

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
    if (!mFormErrors.isEmpty())
      return true;

    // if there is any error in any field also return true
    for (FormField field : mFieldList)
      if (field.hasError())
        return true;

    return false;
  }

  /**
   * Returns whether this form has form level errors. Does not check if fields
   * have any errors. To check if the form and any of the fields have errors use
   *
   * <pre>
   * hasErrors()
   * </pre>
   *
   * @return true or false
   */
  public boolean hasFormErrors()
  {
    return !mFormErrors.isEmpty();
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
   *          The error text. Throws an exception in case of a nonexisting field
   */
  public void setFieldError(String fieldName, String errorText)
  {
    FormField f = getField(fieldName);

    f.setError(errorText);
  }

  /**
   * Return the error for a field.
   *
   * @param fieldName
   *          The field name
   * @return The error or NULL if there was no error. Throws an exception in
   *         case of a nonexisting field
   */
  public String getFieldError(String fieldName)
  {
    FormField field = getField(fieldName);

    return field.getError();
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
   *
   *          Throws an exception in case of a nonexisting field
   */
  public void clearFieldError(String fieldName)
  {

    FormField f = getField(fieldName);
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

  /**
   * Parses a string that was passed by the user in an annotation. Check if the
   * string contains text enclosed in { } and replace that with a string from
   * the resource bundle. Note that currently on { } as the first and last char
   * are supported
   *
   * @param text
   *          the text to parse
   * @return if the text was enclosed in { } return the text in localized from
   *         fetched from a resource bundle
   */
  public String parseAnnotationString(String text)
  {

    if (text.charAt(0) == '{' && text.charAt(text.length() - 1) == '}')
    {
      String key = text.substring(1, text.length() - 1);
      if (mResourceBundle == null)
        return key;
      try
      {
        text = mResourceBundle.getString(key);
      }
      catch (MissingResourceException mre)
      {
        log.warn("Can't find translation for key: " + key);
        return key;
      }

    }

    return text;
  }

  private void initFields(String prefix, Class<?> clazz)
  {

    Field[] classFields = clazz.getDeclaredFields();
    for (Field classField : classFields)
    {
      Annotation[] annotations = classField.getAnnotationsByType(snap.forms.annotations.FollowField.class);
      if (annotations.length > 1)
        throw new SnapException("The @FollowField annotation can only be applied once");
      if (annotations.length == 1)
        initFields(prefix + classField.getName() + ".", classField.getType());
      else
        processAnnotations(prefix, classField);
    }
  }

  private void processAnnotations(String prefix, Field classField)
  {
    Annotation[] annotations = classField.getAnnotations();
    // Loop throw all the annotations on the class and create a field
    // according to its declared kind
    for (Annotation annotation : annotations)
    {
      FormField field = null;
      String fieldName = prefix + classField.getName();
      if (annotation instanceof snap.forms.annotations.TextField)
      {
        snap.forms.annotations.TextField tfa = (snap.forms.annotations.TextField)annotation;
        field = new snap.forms.internal.TextField(this, classField, tfa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.TextArea)
      {
        snap.forms.annotations.TextArea taa = (snap.forms.annotations.TextArea)annotation;
        field = new snap.forms.internal.TextArea(this, classField, taa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.CheckBoxField)
      {
        snap.forms.annotations.CheckBoxField cba = (snap.forms.annotations.CheckBoxField)annotation;
        field = new snap.forms.internal.CheckBoxField(this, classField, cba, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.RadioField)
      {
        snap.forms.annotations.RadioField rfa = (snap.forms.annotations.RadioField)annotation;
        field = new snap.forms.internal.RadioField(this, classField, rfa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.MultiCheckboxField)
      {
        snap.forms.annotations.MultiCheckboxField msfa = (snap.forms.annotations.MultiCheckboxField)annotation;
        field = new snap.forms.internal.MultiCheckboxField(this, classField, msfa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.ListField)
      {
        snap.forms.annotations.ListField ddla = (snap.forms.annotations.ListField)annotation;
        field = new snap.forms.internal.ListField(this, classField, ddla, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.SubmitField)
      {
        snap.forms.annotations.SubmitField sfa = (snap.forms.annotations.SubmitField)annotation;
        field = new snap.forms.internal.SubmitButton(this, classField, sfa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.HiddenField)
      {
        snap.forms.annotations.HiddenField hfa = (snap.forms.annotations.HiddenField)annotation;
        field = new snap.forms.internal.HiddenField(this, classField, hfa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.FileField)
      {
        snap.forms.annotations.FileField ffa = (snap.forms.annotations.FileField)annotation;
        field = new snap.forms.internal.FileField(this, classField, ffa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.PasswordField)
      {
        snap.forms.annotations.PasswordField pwfa = (snap.forms.annotations.PasswordField)annotation;
        field = new snap.forms.internal.PasswordField(this, classField, pwfa, fieldName);
      }
      else if (annotation instanceof snap.forms.annotations.DateField)
      {
        snap.forms.annotations.DateField dfa = (snap.forms.annotations.DateField)annotation;
        field = new snap.forms.internal.DateField(this, classField, dfa, fieldName);
      }
      if (field != null)
      {
        mFieldList.add(field);
        mFieldMap.put(fieldName, field);
      }
    }
  }

  private RequestContext mContext;

  private List<FormField> mFieldList;
  private List<String> mFormErrors;
  private Map<String, FormField> mFieldMap;
  private String mFormName;

  private Locale mLocale = null;
  private ResourceBundle mResourceBundle;

  private boolean mIsAssigned = false;

}
