/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import org.apache.http.client.methods.HttpGet;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpRequestDetails;
import org.bahmni.webclients.oauth.OAuthTokenProvider;

import java.net.URI;


public class OpenMRSOAuthLoginAuthenticator extends OpenMRSBasicLoginAuthenticator {
    private final OAuthTokenProvider tokenProvider;
    private volatile boolean sessionEstablishedBefore = false;

    public OpenMRSOAuthLoginAuthenticator(ConnectionDetails authenticationDetails, OAuthTokenProvider tokenProvider) {
        super(authenticationDetails);
        this.tokenProvider = tokenProvider;
    }

    @Override
    public HttpRequestDetails refreshRequestDetails(URI uri) {
        if (sessionEstablishedBefore) {
            // we are here because the session stopped working: do not trust the cached token either
            tokenProvider.invalidate();
        }
        HttpRequestDetails details = super.refreshRequestDetails(uri);
        sessionEstablishedBefore = true;
        return details;
    }

    @Override
    protected void setCredentials(HttpGet httpGet) {
        httpGet.setHeader("Authorization", "Bearer " + tokenProvider.getAccessToken());
    }
}
