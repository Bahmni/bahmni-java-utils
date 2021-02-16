package org.bahmni.common.config.registration.service.impl;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;
import org.bahmni.common.config.registration.service.RegistrationPageService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RegistrationPageServiceImpl implements RegistrationPageService {

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

        final RegistrationPageJsonMetaData metaData = registrationPageReaderService.read(getRegistrationConfigPath());
        final List<String> mandatoryPersonAttributes = metaData.getConfig().getMandatoryPersonAttributes();

        if(mandatoryPersonAttributes == null) {
            mandatoryAttributes = new ArrayList<>();
        } else {
           mandatoryAttributes = mandatoryPersonAttributes;
        }
    }

    private String getRegistrationConfigPath() {
        return Paths.get(
                OpenmrsUtil.getApplicationDataDirectory(),
                "bahmni_config",
                configRelativePath
        ).toString();
    }
}
