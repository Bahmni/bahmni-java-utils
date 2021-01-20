package org.bahmni.openmrsconnector;

import java.util.Base64;

public class OpenMRSRESTConnection {
    private String server;
    private String userId;
    private String password;

    private static Base64.Encoder base64Encoder = Base64.getEncoder();

    public OpenMRSRESTConnection(String server, String userId, String password) {
        this.server = server;
        this.userId = userId;
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getRestApiUrl() {
        return String.format("http://%s:8080/openmrs/ws/rest/v1/", server);
    }

    public String encodedLogin() {
        return base64Encoder.encodeToString((userId + ":" + password).getBytes());
    }
}