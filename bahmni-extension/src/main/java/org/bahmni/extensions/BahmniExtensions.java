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
