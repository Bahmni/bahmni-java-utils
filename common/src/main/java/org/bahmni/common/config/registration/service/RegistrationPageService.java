/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


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
