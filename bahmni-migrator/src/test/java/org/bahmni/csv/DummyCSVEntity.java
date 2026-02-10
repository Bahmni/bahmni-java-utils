/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.csv;

import org.bahmni.csv.annotation.CSVHeader;

public class DummyCSVEntity extends CSVEntity {
    @CSVHeader(name = "id"  )
    public String id;
    @CSVHeader(name = "name")
    public String name;
    @CSVHeader(name = "caste", optional = true)
    public String caste;

    public DummyCSVEntity() {}

    public DummyCSVEntity(String id, String name) {
        this.id = id;
        this.name = name;
        originalRow(new String[] {id, name});
    }
}
