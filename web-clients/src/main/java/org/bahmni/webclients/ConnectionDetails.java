package org.bahmni.webclients;

public class ConnectionDetails {
    private String authUrl;
    private String userId;
    private String password;
    private int connectionTimeout;
    private int readTimeout;

    public ConnectionDetails(String authUrl, String userId, String password, int connectionTimeout, int readTimeout) {
        this.authUrl = authUrl;
        this.userId = userId;
        this.password = password;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public String getAuthUrl() {
        return authUrl;
    }
}
