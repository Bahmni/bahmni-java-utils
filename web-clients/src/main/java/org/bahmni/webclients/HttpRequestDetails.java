/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.webclients;

import org.apache.http.HttpMessage;

import java.net.URI;

public class HttpRequestDetails {
    private URI uri;
    private ClientCookies clientCookies;
    private HttpHeaders httpHeaders;

    public HttpRequestDetails(URI uri) {
        this(uri, null, null);
    }

    public HttpRequestDetails(URI uri, ClientCookies clientCookies, HttpHeaders httpHeaders) {
        this.uri = uri;
        this.clientCookies = clientCookies == null ? new ClientCookies() : clientCookies;
        this.httpHeaders = httpHeaders == null ? new HttpHeaders() : httpHeaders;
    }

    public URI getUri() {
        return uri;
    }

    public void addDetailsTo(HttpMessage httpMessage) {
        httpHeaders.addTo(httpMessage);
        clientCookies.addTo(httpMessage);
    }

    public ClientCookies getClientCookies() {
        return clientCookies;
    }

    public HttpRequestDetails createNewWith(URI uri) {
        return new HttpRequestDetails(uri, (ClientCookies)clientCookies.clone(), (HttpHeaders)httpHeaders.clone());
    }
}
