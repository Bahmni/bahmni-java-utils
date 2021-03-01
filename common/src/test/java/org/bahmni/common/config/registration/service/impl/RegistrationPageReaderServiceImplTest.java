package org.bahmni.common.config.registration.service.impl;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class RegistrationPageReaderServiceImplTest {

    private URL mockUrl;

    @Before
    public void setUp() throws IOException {
        String jsonPath = "src/test/resources/RegistrationPageConfig.json";
        mockUrl = Paths.get(jsonPath).toUri().toURL();
    }

    @Test
    public void shouldReturnRegistrationPageMetadata() throws IOException {
        final RegistrationPageReaderService registrationPageReaderService = new RegistrationPageReaderServiceImpl();
        final RegistrationPageJsonMetaData registrationPageMetadata = registrationPageReaderService.read(mockUrl);

        assertEquals("bahmni.registration", registrationPageMetadata.getId());
        assertEquals("Bahmni Patient Registration App", registrationPageMetadata.getDescription());
        assertEquals("Admission date", registrationPageMetadata.getConfig().getMandatoryPersonAttributes().get(0));
    }
}
