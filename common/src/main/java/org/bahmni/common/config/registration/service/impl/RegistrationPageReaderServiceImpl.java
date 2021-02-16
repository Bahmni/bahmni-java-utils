package org.bahmni.common.config.registration.service.impl;

import com.google.gson.Gson;
import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

@Component
public class RegistrationPageReaderServiceImpl implements RegistrationPageReaderService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationPageReaderService.class);
    @Override
    public RegistrationPageJsonMetaData read(String jsonPath) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(jsonPath));
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return parse(bufferedReader);
    }

    private RegistrationPageJsonMetaData parse(BufferedReader bufferedReader) {
        try {
            return new Gson().fromJson(bufferedReader, RegistrationPageJsonMetaData.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
