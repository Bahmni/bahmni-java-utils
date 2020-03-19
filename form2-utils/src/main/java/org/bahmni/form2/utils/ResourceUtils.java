package org.bahmni.form2.utils;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {
    public static String convertResourceOutputToString(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Cannot load the provided resource. Unable to continue", e);
        }
    }
}
