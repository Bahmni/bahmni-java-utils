/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


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
