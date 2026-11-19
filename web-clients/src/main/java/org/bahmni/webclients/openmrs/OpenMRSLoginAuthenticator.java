/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpHeaders;
import org.bahmni.webclients.HttpRequestDetails;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OpenMRSLoginAuthenticator implements Authenticator {
    private ConnectionDetails authenticationDetails;
    private HttpRequestDetails previousSuccessfulRequest;

    public OpenMRSLoginAuthenticator(ConnectionDetails authenticationDetails) {
        this.authenticationDetails = authenticationDetails;
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
        ClientCookies clientCookies = OpenMRSSessionLogin.login(authenticationDetails.getAuthUrl(),
                authenticationDetails.getConnectionTimeout(), authenticationDetails.getReadTimeout(), setbasicAuthorizationHeader());
        previousSuccessfulRequest = new HttpRequestDetails(uri, clientCookies, new HttpHeaders());
        return previousSuccessfulRequest;
    }

    private Header setbasicAuthorizationHeader() {
        String credentials = authenticationDetails.getUserId() + ":" + authenticationDetails.getPassword();
        return new BasicHeader(AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
    }
}