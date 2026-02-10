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

import java.util.List;

public class StageResult <T extends CSVEntity> {

    private String stageName;
    private List<T> allCsvEntities;
    private List<FailedRowResult<T>> failedCsvEntities;


    public StageResult(String stageName, List<FailedRowResult<T>> failedCsvEntities, List<T> allCsvEntities) {
        this.stageName = stageName;
        this.failedCsvEntities = failedCsvEntities;
        this.allCsvEntities = allCsvEntities;
    }


    public String getStageName() {
        return stageName;
    }

    public int getFailureCount() {
        if(failedCsvEntities == null)
            return 0;
        return failedCsvEntities.size();
    }

    public int getSuccessCount() {
        if(allCsvEntities == null || allCsvEntities.size() == 0)
            return 0;
        return allCsvEntities.size() - getFailureCount();
    }

    @Override
    public String toString() {
        return "Stage "+ getStageName() + " status: PASS:"+ getSuccessCount() + " FAIL: "+ getFailureCount();
    }

    public List<T> getAllCsvEntities() {
        return allCsvEntities;
    }

    public List<FailedRowResult<T>> getFailedCSVEntities() {
        return failedCsvEntities;
    }
}
