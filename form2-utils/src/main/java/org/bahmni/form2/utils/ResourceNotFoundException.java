package org.bahmni.form2.utils;

import java.io.IOException;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg, IOException e) {
        super(msg, e);
    }
}
