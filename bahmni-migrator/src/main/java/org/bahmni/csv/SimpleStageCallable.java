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
import java.util.concurrent.Callable;

public class SimpleStageCallable implements Callable<StageResult> {

     private SimpleStage simpleStage;
    private List csvEntityList;

    public SimpleStageCallable(SimpleStage simpleStage, List csvEntityList) {
        this.simpleStage = simpleStage;
        this.csvEntityList = csvEntityList;
    }

    @Override
    public StageResult call() throws Exception {
        return simpleStage.execute(csvEntityList);
    }
}
