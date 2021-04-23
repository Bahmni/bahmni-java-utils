package org.bahmni.common.config.registration.service.impl;

import com.google.gson.Gson;
import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;

@Component
public class RegistrationPageReaderServiceImpl implements RegistrationPageReaderService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationPageReaderService.class);
    @Override
    public RegistrationPageJsonMetaData read(URL url) {
       BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            return parse(bufferedReader);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error in parsing registration config", e);
        }
    }

    private RegistrationPageJsonMetaData parse(BufferedReader bufferedReader) {
        try {
            return new Gson().fromJson(bufferedReader, RegistrationPageJsonMetaData.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            try {
                bufferedReader.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
