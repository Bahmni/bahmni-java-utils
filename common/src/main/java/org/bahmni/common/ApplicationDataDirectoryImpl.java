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
