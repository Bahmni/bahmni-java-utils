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

import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bahmni.webclients.WebClientsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class HttpOAuthTokenClient {
    private static final Logger logger = LoggerFactory.getLogger(HttpOAuthTokenClient.class);

    private HttpOAuthTokenClient() {
    }

    public static OAuthToken fetchToken(OAuth2ConnectionDetails credentials) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig(credentials)).build();
        try {
            HttpPost post = new HttpPost(credentials.getTokenUrl());
            post.setHeader(HttpHeaders.AUTHORIZATION, ClientCredentialsRequest.authorizationHeader(credentials));
            post.setHeader(HttpHeaders.ACCEPT, "application/json");
            post.setEntity(new UrlEncodedFormEntity(ClientCredentialsRequest.formParameters(credentials), StandardCharsets.UTF_8));
            return execute(httpClient, post, credentials);
        } catch (IOException e) {
            throw new WebClientsException(String.format("Token request to %s for client '%s' failed: %s",
                    credentials.getTokenUrl(), credentials.getClientId(), e.getMessage()), e);
        } finally {
            closeQuietly(httpClient);
        }
    }

    private static OAuthToken execute(CloseableHttpClient httpClient, HttpPost post, OAuth2ConnectionDetails credentials) throws IOException {
        CloseableHttpResponse response = httpClient.execute(post);
        try {
            int status = response.getStatusLine().getStatusCode();
            String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (status != 200) {
                throw new WebClientsException(String.format("Token request to %s for client '%s' failed with HTTP %d%s",
                        credentials.getTokenUrl(), credentials.getClientId(), status, OAuthTokenResponseParser.describeError(body)));
            }
            OAuthToken token = OAuthTokenResponseParser.parseToken(body, credentials.getTokenUrl());
            logger.info("Obtained OAuth access token for client '{}', valid for {}s", credentials.getClientId(), token.getExpiresInSeconds());
            return token;
        } finally {
            response.close();
        }
    }

    private static RequestConfig requestConfig(OAuth2ConnectionDetails credentials) {
        return RequestConfig.custom()
                .setConnectTimeout(credentials.getConnectionTimeout())
                .setSocketTimeout(credentials.getReadTimeout())
                .setConnectionRequestTimeout(credentials.getReadTimeout())
                .build();
    }

    private static void closeQuietly(CloseableHttpClient httpClient) {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.warn("Could not close token HTTP client: {}", e.getMessage());
        }
    }
}