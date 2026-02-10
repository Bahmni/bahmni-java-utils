/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.extensions;

import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.common.ApplicationDataDirectory;
import org.bahmni.common.ApplicationDataDirectoryImpl;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class BahmniExtensions {

    private static final Logger log = LoggerFactory.getLogger(BahmniExtensions.class);
    public static final String GROOVY_EXTENSION = ".groovy";

    private GroovyClassLoader groovyClassLoader;

    private ApplicationDataDirectory applicationDataDirectory;

    public BahmniExtensions() {
        groovyClassLoader = new GroovyClassLoader();
        applicationDataDirectory = new ApplicationDataDirectoryImpl();
    }

    public Object getExtension(String directory, String fileName) {
        File groovyFile = applicationDataDirectory
                .getFileFromConfig("openmrs" + File.separator + directory + File.separator + fileName);
        if (!groovyFile.exists()) {
            log.error("File not found " + groovyFile.getAbsolutePath());
        } else {
            try {
                Class clazz = groovyClassLoader.parseClass(groovyFile);
                return clazz.newInstance();
            } catch (IOException | IllegalAccessException e) {
                log.error("Problem with the groovy class " + groovyFile, e);
            } catch (InstantiationException e) {
                log.error("The groovy class " + groovyFile + " cannot be instantiated", e);
            }
        }
        return null;
    }
}
