/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.openmrsconnector.response;

import java.util.List;

public class ResourceResponse {
    private List<Resource> results;

    public List<Resource> getResults() {
        return results;
    }

    public void setResults(List<Resource> results) {
        this.results = results;
    }
}