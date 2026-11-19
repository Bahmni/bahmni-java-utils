/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpHeaders;
import org.bahmni.webclients.HttpRequestDetails;

import java.net.URI;
import java.nio.charset.StandardCharsets;

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
                authenticationDetails.getConnectionTimeout(), authenticationDetails.getReadTimeout(), this::setCredentials);
        previousSuccessfulRequest = new HttpRequestDetails(uri, clientCookies, new HttpHeaders());
        return previousSuccessfulRequest;
    }

    protected void setCredentials(HttpGet httpGet) throws AuthenticationException {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(authenticationDetails.getUserId(), authenticationDetails.getPassword());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(new BasicCookieStore());
        context.setCredentialsProvider(credsProvider);
        Header authorizationHeader = new BasicScheme(StandardCharsets.UTF_8).authenticate(credentials, httpGet, context);
        httpGet.setHeader(authorizationHeader);
    }
}