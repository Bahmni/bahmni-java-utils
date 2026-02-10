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
import java.util.List;

class Stages<T extends CSVEntity>  {
    private List<Stage<T>> stages = new ArrayList<>();

    private int index = 0;

    public void addStage(Stage<T> aStage) {
        stages.add(aStage);
    }

    public boolean hasMoreStages() {
        return index < stages.size();
    }

    public Stage<T> nextStage() {
        Stage<T> aStage = stages.get(index);
        index++;
        return aStage;
    }

    public void shutdown() {
        for (Stage<T> stage : stages) {
            stage.closeResources();
        }

    }

    public List<Stage<T>> getStages() {
        return stages;
    }
}
