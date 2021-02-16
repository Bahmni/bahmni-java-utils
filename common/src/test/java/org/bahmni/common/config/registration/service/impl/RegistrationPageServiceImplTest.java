package org.bahmni.common.config.registration.service.impl;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageService;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
public class RegistrationPageServiceImplTest {

    private RegistrationPageService registrationPageService;
    private RegistrationPageJsonMetaData registrationPageMetadata;

    @Mock
    private RegistrationPageReaderService registrationPageReaderService;

    @Before
    public void setUp() {
        final String jsonPath = "src/test/resources/RegistrationPageConfig.json";
        RegistrationPageReaderService registrationPageReaderService2 = new RegistrationPageReaderServiceImpl();
        registrationPageMetadata = registrationPageReaderService2.read(jsonPath);
        when(registrationPageReaderService.read(anyString())).thenReturn(registrationPageMetadata);
        registrationPageService = new RegistrationPageServiceImpl(registrationPageReaderService);
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
