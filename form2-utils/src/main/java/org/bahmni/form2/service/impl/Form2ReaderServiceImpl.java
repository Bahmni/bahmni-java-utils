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

import com.google.gson.Gson;
import org.bahmni.form2.model.Form2JsonMetadata;
import org.bahmni.form2.service.Form2ReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;


@Component
public class Form2ReaderServiceImpl implements Form2ReaderService {

    private static final Logger logger = LoggerFactory.getLogger(Form2ReaderService.class);

    @Override
    public Form2JsonMetadata read(String jsonPath) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(jsonPath));
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return parse(bufferedReader);
    }

    private Form2JsonMetadata parse(BufferedReader metadataReader) {
        try {
            return new Gson().fromJson(metadataReader, Form2JsonMetadata.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
