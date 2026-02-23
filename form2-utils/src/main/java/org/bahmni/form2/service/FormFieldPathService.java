/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.form2.service;

import java.util.List;

public interface FormFieldPathService {

    /**
     * @param orderedControlNames Eg: Vitals, BMI, Height as list items when Height
     *                            control is under BMI section and form name is Vitals
     * @return form field path Eg: Vitals.1/2-0
     */
    String getFormFieldPath(List<String> orderedControlNames);

    /**
     * @param orderedControlNames Eg: Vitals, BMI, Height as list items when Height
     *                            control is under BMI section and form name is Vitals
     * @return true when obs is multi select; otherwise false
     */
    boolean isMultiSelectObs(List<String> orderedControlNames);

    /**
     * @param orderedControlNames Eg: Vitals, BMI, Height as list items when Height
     *                            control is under BMI section and form name is Vitals
     * @return true when obs is mandatory; otherwise false
     */
    boolean isMandatory(List<String> orderedControlNames);

    /**
     * @param orderedControlNames Eg: Vitals, BMI, Height as list items when Height
     *                            control is under BMI section and form name is Vitals
     * @return true when obs is add more; otherwise false
     */
    boolean isAddmore(List<String> orderedControlNames);

    /**
     * @param orderedControlNames Eg: Vitals, BMI, Height as list items when Height
     *                            control is under BMI section and form name is Vitals
     * @return true when date obs is allowing future dates; otherwise false
     */
    boolean isAllowFutureDates(List<String> orderedControlNames);

    /**
     * @param orderedControlNames Eg: Vitals, BMI, Height as list items when Height
     *                            control is under BMI section and form name is Vitals
     * @return true when CSV Header is valid; otherwise false
     */
    boolean isValidCSVHeader(List<String> orderedControlNames);
}
