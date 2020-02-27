package org.bahmni.module.service;

import java.util.List;

public interface FormFieldPathService {

    /**
     * @param orderedControlNames Eg: Vitals, BMI, Height as list items when Height
     *                            control is under BMI section and form name is Vitals
     * @return form field path Eg: Vitals.1/2-0
     */
    String getFormFieldPath(List<String> orderedControlNames);
}
