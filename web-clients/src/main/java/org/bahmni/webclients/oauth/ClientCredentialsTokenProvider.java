/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.oauth;

import org.bahmni.webclients.OAuthToken;

import org.bahmni.webclients.OAuth2ConnectionDetails;

import java.time.Clock;


public class ClientCredentialsTokenProvider {
    private final OAuth2ConnectionDetails credentials;
    private final Clock clock;
    private String accessToken;
    private long refreshAtMillis;

    public ClientCredentialsTokenProvider(OAuth2ConnectionDetails credentials) {
        this(credentials, Clock.systemUTC());
    }

    ClientCredentialsTokenProvider(OAuth2ConnectionDetails credentials, Clock clock) {
        this.credentials = credentials;
        this.clock = clock;
    }

    public synchronized String getAccessToken() {
        if (accessToken == null || clock.millis() >= refreshAtMillis) {
            cache(HttpOAuthTokenClient.fetchToken(credentials));
        }
        return accessToken;
    }

    public synchronized void invalidate() {
        accessToken = null;
    }

    private void cache(OAuthToken token) {
        long lifetimeSeconds = token.getExpiresInSeconds();
        long skewSeconds = Math.min(credentials.getExpirySkewSeconds(), lifetimeSeconds / 2);
        this.accessToken = token.getAccessToken();
        this.refreshAtMillis = clock.millis() + (lifetimeSeconds - skewSeconds) * 1000;
    }
}