package com.proficiosoftware.snap.forms;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proficiosoftware.snap.forms.internal.FormField;

/**
 * Subclass this class to create a new form. You can add any field that derives
 * from FormField. Form fields will be automatically created by the form. Only
 * fields that are public and have the annotation @Attributes will be used in
 * the HTML. If you need to exclude a field set the annotation @Exclude
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
  }

  public void init()
  {
    mFieldList.clear();
    
    Field[] classFields = getClass().getFields();
    for (Field classField : classFields)
    {
      Annotation[] annotations = classField.getAnnotations();

      // TODO: could cache the reflection field object in the Html Field
      // represenation obj
      for (Annotation annotation : annotations)
      {
        if (annotation instanceof com.proficiosoftware.snap.forms.annotations.TextField)
        {
          com.proficiosoftware.snap.forms.annotations.TextField an = (com.proficiosoftware.snap.forms.annotations.TextField)annotation;
          mFieldList
              .add(new com.proficiosoftware.snap.forms.internal.TextField(an
                  .id(), classField.getName(), an.label()));
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.PasswordField)
        {
          com.proficiosoftware.snap.forms.annotations.PasswordField pw = (com.proficiosoftware.snap.forms.annotations.PasswordField)annotation;
          mFieldList
              .add(new com.proficiosoftware.snap.forms.internal.PasswordField(
                  pw.id(), classField.getName(), pw.label()));
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.FileField)
        {
          com.proficiosoftware.snap.forms.annotations.FileField ff = (com.proficiosoftware.snap.forms.annotations.FileField)annotation;
          mFieldList
              .add(new com.proficiosoftware.snap.forms.internal.FileField(ff
                  .id(), classField.getName(), ff.label()));
        }
        else if (annotation instanceof com.proficiosoftware.snap.forms.annotations.SubmitField)
        {
          com.proficiosoftware.snap.forms.annotations.SubmitField sf = (com.proficiosoftware.snap.forms.annotations.SubmitField)annotation;
          mFieldList.add(new com.proficiosoftware.snap.forms.SubmitButton(sf
              .id(), classField.getName()));
        }
      }
    }
  }

  public void init(Map<String, String[]> defaults)
  {
    if (defaults == null)
      return;
    
    init();

    Field[] classFields = getClass().getFields();
    for (Field classField : classFields)
    {
      String fieldName = classField.getName();
      String values[] = defaults.get(fieldName);
      if (values != null && values[0] != null)
        try
        {
          classField.set(this, values[0]);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
          log.debug("Can't set value for field: " + fieldName, e);
        }
    }

  }

  public String render()
  {

    StringBuilder builder = new StringBuilder();
    for (FormField field : mFieldList)
    {
      String value = "";
      try
      {
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
        builder.append("<p style=\"field-error\">" + field.getErrorText()
            + "</p>");

    }
    return builder.toString();
  }

  public boolean validate()
  {
    return true;
  }

  public boolean hasErrors()
  {
    if (mErrorText != null)
      return true;

    for (FormField field : mFieldList)
      if (field.hasError())
        return true;

    return false;
  }

  public String getErrorText()
  {
    return mErrorText;
  }

  public void setErrorText(String errorText)
  {
    mErrorText = errorText;
  }

  public void clearErrors()
  {
    mErrorText = null;
    for (FormField field : mFieldList)
      field.clearError();

  }

  private List<FormField> mFieldList;
  private String mErrorText = null;
}
