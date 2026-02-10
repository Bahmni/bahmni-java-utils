/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.form2.service.impl;

import org.bahmni.form2.model.Form2JsonMetadata;
import org.bahmni.form2.service.Form2ReaderService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Form2ReaderServiceImplTest {

    @Test
    public void shouldReturnForm2JsonMetadata() {

        final Form2ReaderService form2ReaderService = new Form2ReaderServiceImpl();
        final String jsonPath = "src/test/resources/Vitals_1.json";
        final Form2JsonMetadata form2JsonMetadata = form2ReaderService.read(jsonPath);

        assertEquals("Vitals", form2JsonMetadata.getName());
        assertEquals(1, form2JsonMetadata.getControls().size());
        assertEquals("obsControl", form2JsonMetadata.getControls().get(0).getType());
        assertEquals("Height", form2JsonMetadata.getControls().get(0).getConcept().getName());
    }
}
