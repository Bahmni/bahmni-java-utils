/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients;

public class OAuth2ConnectionDetails {

    private final String authUrl;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final int connectionTimeout;
    private final int readTimeout;

    public OAuth2ConnectionDetails(String authUrl, String tokenUrl, String clientId, String clientSecret, String scope,
                                  int connectionTimeout, int readTimeout) {
        this.authUrl = authUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public String toString() {
        return "OAuth2ConnectionDetails{tokenUrl='" + tokenUrl + "', clientId='" + clientId + "', clientSecret='****'}";
    }
}
