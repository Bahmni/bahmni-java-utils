package org.bahmni.form2.model;

import java.util.List;

public class Form2JsonMetadata {
    private List<Control> controls;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Control> getControls() {
        return controls;
    }

    public void setControls(List<Control> controls) {
        this.controls = controls;
    }
}
