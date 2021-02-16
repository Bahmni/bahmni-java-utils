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
