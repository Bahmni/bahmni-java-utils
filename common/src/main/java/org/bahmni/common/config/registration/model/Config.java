/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.common.config.registration.model;

import java.util.List;

public class Config {
    private List<String> mandatoryPersonAttributes;

    public List<String> getMandatoryPersonAttributes() {
        return mandatoryPersonAttributes;
    }

    public void setMandatoryPersonAttributes(List<String> mandatoryPersonAttributes) {
        this.mandatoryPersonAttributes = mandatoryPersonAttributes;
    }
}
