package org.bahmni.common.config.registration.service;

import java.util.List;

public interface RegistrationPageService {

    public List<String> getMandatoryAttributes();

    public boolean isMandatoryAttribute(String attribute);
}
