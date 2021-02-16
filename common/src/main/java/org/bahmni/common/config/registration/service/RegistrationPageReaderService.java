package org.bahmni.common.config.registration.service;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;

public interface RegistrationPageReaderService {

    RegistrationPageJsonMetaData read(String jsonPath);
}
