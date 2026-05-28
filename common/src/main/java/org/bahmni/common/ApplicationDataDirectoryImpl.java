/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.common;

import org.openmrs.util.OpenmrsUtil;


import java.io.File;

public class ApplicationDataDirectoryImpl implements ApplicationDataDirectory {

    @Override
    public File getFile(String relativePath) {
        return new File(OpenmrsUtil.getApplicationDataDirectory() + relativePath);
    }

    @Override
    public File getFileFromConfig(String relativePath) {
        return new File(OpenmrsUtil.getApplicationDataDirectory(),"bahmni_config"+ File.separator+relativePath);
    }
}
