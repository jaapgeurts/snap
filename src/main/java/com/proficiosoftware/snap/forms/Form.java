package com.proficiosoftware.snap.forms;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proficiosoftware.snap.forms.internal.FormField;
import com.proficiosoftware.snap.http.HttpRequest;

/**
 * Subclass this class to create a new form. You can add any field and annotate
 * it with the html input type you wish to use. Only fields that have the
 * annotation in forms.annotations will be used in the HTML.
 * 
 * 
 * @author Jaap Geurts
 *
 */
public class Form
{

  final Logger log = LoggerFactory.getLogger(Form.class);

  public Form()
  {
    mFieldList = new ArrayList<FormField>();
    mFieldMap = new HashMap<String, FormField>();
  }

  public void init()
  {
    mFieldList.clear();
    mFieldMap.clear();

    Field[] classFields = getClass().getFields();
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
        if (annotation instanceof com.proficiosoftware.snap.forms.annotations.TextField)
        {
          com.proficiosoftware.snap.forms.annotations.TextField an = (com.proficiosoftware.snap.forms.annotations.TextField)annotation;
          field = new com.proficiosoftware.snap.forms.internal.TextField(
              an.id(), fieldName, an.label());
        }
        if (annotation instanceof com.proficiosoftware.snap.forms.annotations.TextArea)
        {
          com.proficiosoftware.snap.forms.annotations.TextArea an = (com.proficiosoftware.snap.forms.annotations.TextArea)annotation;
          field = new com.proficiosoftware.snap.forms.internal.TextArea(
              an.id(), fieldName, an.label());
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.DropDownList)
        {
          com.proficiosoftware.snap.forms.annotations.DropDownList ddl = (com.proficiosoftware.snap.forms.annotations.DropDownList)annotation;
          Field listField;
          try
          {
            listField = getClass().getField(ddl.optionList());
          }
          catch (NoSuchFieldException | SecurityException e)
          {
            throw new AnnotationFormatError(
                "optionList value not specified or invalid or not accessible. optionList value must name a field in the object of type List<Object>",
                e);
          }
          Object list;
          try
          {
            list = listField.get(this);
            if (list instanceof List<?>)
            {
              field = new com.proficiosoftware.snap.forms.internal.DropDownList(
                  ddl.id(), fieldName, ddl.label(), (List<?>)list);
            }
            else
            {
              throw new AnnotationFormatError(
                  "optionList value is not of type List<?>.");
            }
          }
          catch (IllegalArgumentException | IllegalAccessException e)
          {
            throw new AnnotationFormatError(
                "optionList value invalid or not accessible. optionList value must name a field in the object of type List<Object>",
                e);
          }
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.SubmitField)
        {
          com.proficiosoftware.snap.forms.annotations.SubmitField sf = (com.proficiosoftware.snap.forms.annotations.SubmitField)annotation;
          field = new com.proficiosoftware.snap.forms.SubmitButton(sf.id(),
              fieldName);
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.HiddenField)
        {
          com.proficiosoftware.snap.forms.annotations.HiddenField hf = (com.proficiosoftware.snap.forms.annotations.HiddenField)annotation;
          field = new com.proficiosoftware.snap.forms.internal.HiddenField(
              hf.id(), fieldName);
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.FileField)
        {
          com.proficiosoftware.snap.forms.annotations.FileField ff = (com.proficiosoftware.snap.forms.annotations.FileField)annotation;
          field = new com.proficiosoftware.snap.forms.internal.FileField(
              ff.id(), fieldName, ff.label());
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.PasswordField)
        {
          com.proficiosoftware.snap.forms.annotations.PasswordField pw = (com.proficiosoftware.snap.forms.annotations.PasswordField)annotation;
          field = new com.proficiosoftware.snap.forms.internal.PasswordField(
              pw.id(), fieldName, pw.label());
        }
        if (field != null)
        {
          mFieldList.add(field);
          mFieldMap.put(fieldName, field);
        }
      }
    }
  }

  public void init(HttpRequest request)
  {
    if (request == null)
      return;

    init();

    Map<String, String[]> defaults = request.getParams();

    Field[] classFields = getClass().getFields();
    for (Field classField : classFields)
    {
      String fieldName = classField.getName();
      try
      {
        if (classField.getType().isAssignableFrom(Part.class))
        {
          classField.set(this, request.getRequest().getPart(fieldName));
        }
        else
        {
          String values[] = defaults.get(fieldName);
          if (values != null && values[0] != null)
          {
            classField.set(this, values[0]);
          }
        }
      }
      catch (IllegalArgumentException | IllegalAccessException | IOException
          | ServletException e)
      {
        log.debug("Can't set value for field: " + fieldName, e);
      }
    }

  }

  public String render()
  {

    StringBuilder builder = new StringBuilder();
    if (mFormError != null && !"".equals(mFormError))
    {
      builder.append("<p class=\"form-error\">");
      builder.append(getFormError());
      builder.append("</p>");
    }
    for (FormField field : mFieldList)
    {
      String value = "";
      try
      {
        // TODO: consider passing the actual object
        Field classField = getClass().getField(field.getName());
        Object val = classField.get(this);
        if (val != null)
          value = val.toString();
      }
      catch (NoSuchFieldException | IllegalArgumentException
          | IllegalAccessException e)
      {
        log.debug("FormField found without class field.", e);
      }
      builder.append(field.render(value));
      if (field.hasError())
      {
        builder.append("<p class=\"field-error\">");
        builder.append(field.getError());
        builder.append("</p>");
      }
    }
    return builder.toString();
  }

  public boolean validate()
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

  public boolean hasErrors()
  {
    if (mFormError != null)
      return true;

    for (FormField field : mFieldList)
      if (field.hasError())
        return true;

    return false;
  }

  public String getFormError()
  {
    return mFormError;
  }

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
}
