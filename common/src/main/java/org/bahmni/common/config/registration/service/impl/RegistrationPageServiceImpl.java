package org.bahmni.common.config.registration.service.impl;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;
import org.bahmni.common.config.registration.service.RegistrationPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RegistrationPageServiceImpl implements RegistrationPageService {

    private String host;
    private String protocol;

    private String configRelativePath = "openmrs/apps/registration/app.json";

    private RegistrationPageReaderService registrationPageReaderService;

    private List<String> mandatoryAttributes = null;

    @Autowired
    public RegistrationPageServiceImpl(RegistrationPageReaderService registrationPageReaderService) {
        this.registrationPageReaderService = registrationPageReaderService;
    }

    @Override
    public List<String> getMandatoryAttributes() {
        if(mandatoryAttributes==null) {
            initializeMandatoryAttributes();
        }
        return mandatoryAttributes;
    }

    @Override
    public boolean isMandatoryAttribute(String attribute) {
        if(mandatoryAttributes==null) {
            initializeMandatoryAttributes();
        }
        return mandatoryAttributes.contains(attribute);
    }

    private void initializeMandatoryAttributes() {
        try {
            final RegistrationPageJsonMetaData metaData = registrationPageReaderService.read(getRegistrationConfigPathUrl());
            final List<String> mandatoryPersonAttributes = metaData.getConfig().getMandatoryPersonAttributes();

            if(mandatoryPersonAttributes == null) {
                mandatoryAttributes = new ArrayList<>();
            } else {
                mandatoryAttributes = mandatoryPersonAttributes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private URL getRegistrationConfigPathUrl() throws MalformedURLException {
        return new URL(protocol, host, getConfigJsonPath());
    }

    private String getConfigJsonPath() {
        return Paths.get(
                "/bahmni_config",
                configRelativePath
        ).toString();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }
}
