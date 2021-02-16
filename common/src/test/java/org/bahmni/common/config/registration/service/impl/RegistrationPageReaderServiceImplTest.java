package org.bahmni.common.config.registration.service.impl;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegistrationPageReaderServiceImplTest {

    @Test
    public void shouldReturnRegistrationPageMetadata() {

        final RegistrationPageReaderService registrationPageReaderService = new RegistrationPageReaderServiceImpl();
        final String jsonPath = "src/test/resources/RegistrationPageConfig.json";
        final RegistrationPageJsonMetaData registrationPageMetadata = registrationPageReaderService.read(jsonPath);

        assertEquals("bahmni.registration", registrationPageMetadata.getId());
        assertEquals("Bahmni Patient Registration App", registrationPageMetadata.getDescription());
        assertEquals("Admission date", registrationPageMetadata.getConfig().getMandatoryPersonAttributes().get(0));
    }
}
