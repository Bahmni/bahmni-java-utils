package org.bahmni.webclients;

public class WebClientsException extends RuntimeException {
    public WebClientsException(Throwable cause) {
        super(cause);
    }

    public WebClientsException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebClientsException(String message) {
        super(message);
    }
}