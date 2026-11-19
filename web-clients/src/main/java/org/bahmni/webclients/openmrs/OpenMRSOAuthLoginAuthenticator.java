/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import org.apache.http.message.BasicHeader;
import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.HttpHeaders;
import org.bahmni.webclients.HttpRequestDetails;
import org.bahmni.webclients.OAuth2ConnectionDetails;
import org.bahmni.webclients.oauth.HttpOAuthTokenClient;

import java.net.URI;

public class OpenMRSOAuthLoginAuthenticator implements Authenticator {
    private final OAuth2ConnectionDetails connectionDetails;
    private HttpRequestDetails previousSuccessfulRequest;

    public OpenMRSOAuthLoginAuthenticator(OAuth2ConnectionDetails connectionDetails) {
        this.connectionDetails = connectionDetails;
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
        ClientCookies clientCookies = OpenMRSSessionLogin.login(connectionDetails.getAuthUrl(),
                connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout(),
                new BasicHeader(AUTHORIZATION, "Bearer " + HttpOAuthTokenClient.fetchToken(connectionDetails)));
        previousSuccessfulRequest = new HttpRequestDetails(uri, clientCookies, new HttpHeaders());
        return previousSuccessfulRequest;
    }
}