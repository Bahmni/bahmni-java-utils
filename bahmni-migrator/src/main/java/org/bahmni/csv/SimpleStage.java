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

import org.bahmni.csv.exception.MigrationException;

import java.util.List;

public interface SimpleStage<T extends CSVEntity> {
    /**
     * Name of the Stage
     */
    public String getName();

    /**
     * Indicates whether this stage can run in parallel
     * @return true if YES
     */
    public boolean canRunInParallel();

    /**
     * Main logic to process the whole data.
     *
     * @param csvEntityList
     * @return A summary of the stage result
     * @throws MigrationException To indicate that migration should be aborted
     */
    public StageResult execute(List<T> csvEntityList) throws MigrationException;

}
