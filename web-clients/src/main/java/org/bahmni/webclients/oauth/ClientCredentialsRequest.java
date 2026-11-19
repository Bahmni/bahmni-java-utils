/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.oauth;

import org.bahmni.webclients.OAuth2ConnectionDetails;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

final class ClientCredentialsRequest {

    private ClientCredentialsRequest() {
    }

    static List<NameValuePair> formParameters(OAuth2ConnectionDetails credentials) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
        String scope = credentials.getScope();
        if (scope != null && !scope.trim().isEmpty()) {
            params.add(new BasicNameValuePair("scope", scope.trim()));
        }
        return params;
    }

    static String authorizationHeader(OAuth2ConnectionDetails credentials) throws IOException {
        String id = URLEncoder.encode(credentials.getClientId(), "UTF-8");
        String secret = URLEncoder.encode(credentials.getClientSecret(), "UTF-8");
        return "Basic " + Base64.getEncoder().encodeToString((id + ":" + secret).getBytes(StandardCharsets.UTF_8));
    }
}
