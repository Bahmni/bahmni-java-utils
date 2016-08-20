package org.bahmni.common;

import java.io.File;

public interface ApplicationDataDirectory {
    File getFile(String relativePath);

    File getFileFromConfig(String relativePath);
}
