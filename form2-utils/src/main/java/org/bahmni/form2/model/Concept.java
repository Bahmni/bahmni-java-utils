/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.form2.model;

import java.util.ArrayList;
import java.util.List;

public class Concept {
    private String name;
    //TODO: uuid can be removed
    private String uuid;
    private String datatype;
    private List<ConceptAnswer> answers = new ArrayList<>();

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ConceptAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ConceptAnswer> answers) {
        this.answers = answers;
    }
}
