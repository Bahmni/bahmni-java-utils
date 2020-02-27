package org.bahmni.module.utils;

import java.io.IOException;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg, IOException e) {
        super(msg, e);
    }
}
