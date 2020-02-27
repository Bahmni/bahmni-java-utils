package org.bahmni.module.service;

import java.util.Map;

public interface Form2Service {

    Map<String, String> getAllLatestFormPaths();

    Map<String, Integer> getFormNamesWithLatestVersionNumber();

    String getFormPath(String formName);

    int getFormLatestVersion(String formName);

}
