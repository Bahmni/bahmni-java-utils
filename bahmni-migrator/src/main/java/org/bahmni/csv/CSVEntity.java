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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// All fields have to be String
public abstract class CSVEntity {
    private List<String> originalRow = new ArrayList<>();

    public String[] getRowWithErrorColumn(String errorMessage) {
        List<String> tempList = new ArrayList<>();
        tempList.addAll(originalRow);
        tempList.add(errorMessage);

        return tempList.toArray(new String[]{});
    }

    public void originalRow(String[] aRow) {
        List<String> originalRow = new ArrayList<>(Arrays.asList(aRow));
        this.originalRow = originalRow;
    }

    public List<String> getOriginalRow() {
        return originalRow;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.getOriginalRow().toArray(new String[]{}));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CSVEntity csvEntity = (CSVEntity) o;

        if (originalRow != null ? !originalRow.equals(csvEntity.originalRow) : csvEntity.originalRow != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return originalRow != null ? originalRow.hashCode() : 0;
    }
}
