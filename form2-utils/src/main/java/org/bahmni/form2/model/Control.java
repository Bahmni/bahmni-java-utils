/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.form2.model;

import java.util.List;

public class Control {

    private ControlProperties properties;
    private Concept concept;
    private ControlLabel label;
    private List<Control> controls;
    private String type;
    private String id;

    public ControlLabel getLabel() {
        return label;
    }

    public void setLabel(ControlLabel label) {
        this.label = label;
    }

    public List<Control> getControls() {
        return controls;
    }

    public void setControls(List<Control> controls) {
        this.controls = controls;
    }

    public ControlProperties getProperties() {
        return properties;
    }

    public void setProperties(ControlProperties properties) {
        this.properties = properties;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
