package org.bahmni.common.config.registration.service;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;

import java.io.IOException;
import java.net.URL;

public interface RegistrationPageReaderService {

    RegistrationPageJsonMetaData read(URL url) throws IOException;
}
