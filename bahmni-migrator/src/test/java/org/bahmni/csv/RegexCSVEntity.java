/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.csv;

import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;

import java.util.List;

public class RegexCSVEntity extends CSVEntity{

    @CSVHeader(name = "id"  )
    public String id;
    @CSVHeader(name = "name")
    public String name;
    @CSVRegexHeader(pattern="Obs.*")
    public List<KeyValue> observations;

    public RegexCSVEntity() {}

    public RegexCSVEntity(String id, String name) {
        this.id = id;
        this.name = name;
        originalRow(new String[] {id, name});
    }
}
