/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.form2.model;

public class ConceptAnswer {

    private String displayString;
    private String translationKey;

    public String getDisplayString() {
        return displayString;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }
}
