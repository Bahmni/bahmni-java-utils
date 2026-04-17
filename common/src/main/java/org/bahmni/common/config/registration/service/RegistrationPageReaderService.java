/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.common.config.registration.service;

import org.bahmni.common.config.registration.model.RegistrationPageJsonMetaData;

import java.io.IOException;
import java.net.URL;

public interface RegistrationPageReaderService {

    RegistrationPageJsonMetaData read(URL url) throws IOException;
}
