/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bahmni.webclients.*;
import org.bahmni.webclients.oauth.ClientCredentialsTokenProvider;
import org.bahmni.webclients.OAuth2ConnectionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenMRSOAuthLoginAuthenticator implements Authenticator {
    private static Logger logger = LoggerFactory.getLogger(OpenMRSOAuthLoginAuthenticator.class);
    private static final String SESSION_ID_KEY = "JSESSIONID";
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
        String responseText = null;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionDetails.getConnectionTimeout())
                .setSocketTimeout(connectionDetails.getReadTimeout())
                .setConnectionRequestTimeout(connectionDetails.getReadTimeout())
                .build();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpGet httpGet = new HttpGet(connectionDetails.getAuthUrl());
        try {
            httpGet.setHeader("Authorization", "Bearer " + tokenProvider.getAccessToken());
            logger.info(String.format("Executing request: %s", httpGet.getRequestLine()));
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 204) {
                throw new WebClientsException("Two factor authentication is enabled, Please enable required privilege for the user");
            }
            if (entity != null) {
                InputStream content = entity.getContent();
                responseText = IOUtils.toString(content);
            }
            logger.info(String.format("Authentication response: %s", responseText));
            EntityUtils.consume(entity);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            OpenMRSAuthenticationResponse openMRSResponse = objectMapper.readValue(responseText, OpenMRSAuthenticationResponse.class);
            confirmAuthenticated(openMRSResponse);
            ClientCookies clientCookies = new ClientCookies();
            clientCookies.put(SESSION_ID_KEY, extractSessionId(response.getHeaders("Set-Cookie")[0].getValue()));
            previousSuccessfulRequest = new HttpRequestDetails(uri, clientCookies, new HttpHeaders());
            sessionEstablishedBefore = true;
            return previousSuccessfulRequest;
        } catch (Exception e) {
            throw new WebClientsException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String extractSessionId(String cookie) {
        if (cookie == null) return null;
        Pattern pattern = Pattern.compile("\\bJSESSIONID=([A-Z0-9]{32})");
        Matcher matcher = pattern.matcher(cookie);
        if (matcher.find()) return matcher.group(1);
        throw new WebClientsException("No Matching SessionID in the Response Cookie");
    }

    private void confirmAuthenticated(OpenMRSAuthenticationResponse openMRSResponse) {
        if (!openMRSResponse.isAuthenticated()) {
            logger.error("Could not authenticate with OpenMRS. ");
            throw new WebClientsException("Could not authenticate with OpenMRS");
        }
    }
}