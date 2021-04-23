package org.bahmni.common.config.registration.service;

import java.util.List;

public interface RegistrationPageService {

    public List<String> getMandatoryAttributes();

    public boolean isMandatoryAttribute(String attribute);

    public void setHost(String host);

    public String getHost();

    public void setProtocol(String protocol);

    public String getProtocol();
}
