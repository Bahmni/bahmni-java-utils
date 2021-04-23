package org.bahmni.common.config.registration.service.impl;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageService;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


public class RegistrationPageServiceImplTest {

    private RegistrationPageJsonMetaData registrationPageMetadata;
    private RegistrationPageService registrationPageService;

    @Mock
    private RegistrationPageReaderService registrationPageReaderService;

    private URL mockUrl;

    @Before
    public void setUp() throws IOException {
        String jsonPath = "src/test/resources/RegistrationPageConfig.json";
        mockUrl = Paths.get(jsonPath).toUri().toURL();
        RegistrationPageReaderService registrationPageReaderService2 = new RegistrationPageReaderServiceImpl();
        registrationPageMetadata = registrationPageReaderService2.read(mockUrl);
        registrationPageReaderService = mock(RegistrationPageReaderService.class);

        when(registrationPageReaderService.read(any(URL.class))).thenReturn(registrationPageMetadata);
        registrationPageService = new RegistrationPageServiceImpl(registrationPageReaderService);
        registrationPageService.setProtocol("https");
        registrationPageService.setHost("localhost");
    }

    @Test
    public void shouldReturnMandatoryAttributesInRegistrationPageConfig() {
        assertEquals("Admission date", registrationPageService.getMandatoryAttributes().get(0));
    }

    @Test
    public void shouldReturnTrueIfMandatoryAttributeIsPresentRegistrationPageConfig() {
        assertTrue(registrationPageService.isMandatoryAttribute("Admission date"));
    }

    @Test
    public void shouldReturnFalseIfMandatoryAttributeIsPresentRegistrationPageConfig() {
        assertFalse(registrationPageService.isMandatoryAttribute("Not a mandatory attribute"));
    }
}
