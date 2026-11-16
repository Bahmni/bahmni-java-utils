/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.oauth;

public class OAuthClientCredentials {
    public static final int DEFAULT_EXPIRY_SKEW_SECONDS = 30;

    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final int connectionTimeout;
    private final int readTimeout;
    private final int expirySkewSeconds;

    public OAuthClientCredentials(String tokenUrl, String clientId, String clientSecret, String scope,
                                  int connectionTimeout, int readTimeout, int expirySkewSeconds) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.expirySkewSeconds = expirySkewSeconds;
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

    public int getExpirySkewSeconds() {
        return expirySkewSeconds;
    }

    @Override
    public String toString() {
        return "OAuthClientCredentials{tokenUrl='" + tokenUrl + "', clientId='" + clientId + "', clientSecret='****'}";
    }
}
