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
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.WebClientsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class OpenMRSSessionLogin {
    private static final Logger logger = LoggerFactory.getLogger(OpenMRSSessionLogin.class);
    private static final String SESSION_ID_KEY = "JSESSIONID";
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("\\bJSESSIONID=([A-Z0-9]{32})");

    interface AuthorizationSetter {
        void setAuthorization(HttpGet request) throws AuthenticationException;
    }

    private OpenMRSSessionLogin() {
    }

    static ClientCookies login(String sessionUrl, int connectionTimeout, int readTimeout, AuthorizationSetter authorization) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(readTimeout)
                .setConnectionRequestTimeout(readTimeout)
                .build();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        try {
            HttpGet httpGet = new HttpGet(sessionUrl);
            authorization.setAuthorization(httpGet);
            logger.info(String.format("Executing request: %s", httpGet.getRequestLine()));
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getStatusCode() == 204) {
                    throw new WebClientsException("Two factor authentication is enabled, Please enable required privilege for the user");
                }
                String responseText = null;
                if (entity != null) {
                    InputStream content = entity.getContent();
                    responseText = IOUtils.toString(content);
                }
                EntityUtils.consume(entity);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                OpenMRSAuthenticationResponse openMRSResponse = objectMapper.readValue(responseText, OpenMRSAuthenticationResponse.class);
                confirmAuthenticated(openMRSResponse);
                logger.debug("OMRS Authentication successful");
                ClientCookies clientCookies = new ClientCookies();
                clientCookies.put(SESSION_ID_KEY, extractSessionId(response.getHeaders("Set-Cookie")[0].getValue()));
                return clientCookies;
            }
        } catch (Exception e) {
            throw new WebClientsException(e);
        } finally {
            closeQuietly(httpClient);
        }
    }

    private static String extractSessionId(String cookie) {
        if (cookie == null) return null;
        Matcher matcher = SESSION_ID_PATTERN.matcher(cookie);
        if (matcher.find()) return matcher.group(1);
        throw new WebClientsException("No Matching SessionID in the Response Cookie");
    }

    private static void confirmAuthenticated(OpenMRSAuthenticationResponse openMRSResponse) {
        if (!openMRSResponse.isAuthenticated()) {
            logger.error("Could not authenticate with OpenMRS. ");
            throw new WebClientsException("Could not authenticate with OpenMRS");
        }
    }

    private static void closeQuietly(CloseableHttpClient httpClient) {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.warn("Could not close OpenMRS session HTTP client: {}", e.getMessage());
        }
    }
}