/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.HttpHeaders;
import org.bahmni.webclients.HttpRequestDetails;
import org.bahmni.webclients.OAuth2ConnectionDetails;
import org.bahmni.webclients.oauth.ClientCredentialsTokenProvider;

import java.net.URI;

public class OpenMRSOAuthLoginAuthenticator implements Authenticator {
    private final OAuth2ConnectionDetails connectionDetails;
    private final ClientCredentialsTokenProvider tokenProvider;
    private HttpRequestDetails previousSuccessfulRequest;
    private volatile boolean sessionEstablishedBefore = false;

    public OpenMRSOAuthLoginAuthenticator(OAuth2ConnectionDetails connectionDetails) {
        this.connectionDetails = connectionDetails;
        this.tokenProvider = new ClientCredentialsTokenProvider(connectionDetails);
    }

    @Override
    public HttpRequestDetails getRequestDetails(URI uri) {
        if (previousSuccessfulRequest == null) {
            return refreshRequestDetails(uri);
        }
        return previousSuccessfulRequest.createNewWith(uri);
    }

    @Override
    public HttpRequestDetails refreshRequestDetails(URI uri) {
        if (sessionEstablishedBefore) {
            tokenProvider.invalidate();
        }
        ClientCookies clientCookies = OpenMRSSessionLogin.login(connectionDetails.getAuthUrl(),
                connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout(),
                request -> request.setHeader("Authorization", "Bearer " + tokenProvider.getAccessToken()));
        previousSuccessfulRequest = new HttpRequestDetails(uri, clientCookies, new HttpHeaders());
        sessionEstablishedBefore = true;
        return previousSuccessfulRequest;
    }
}