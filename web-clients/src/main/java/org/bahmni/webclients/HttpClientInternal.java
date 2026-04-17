/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.bahmni.webclients.ObjectMapperRepository.objectMapper;

public class HttpClientInternal {
    private int connectTimeout;
    private int readTimeout;
    private CloseableHttpClient closeableHttpClient;
    private PoolingHttpClientConnectionManager connectionManager;

    HttpClientInternal(int connectionTimeout, int readTimeout) {
        this.connectTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    HttpClientInternal(int connectionTimeout, int readTimeout, PoolingHttpClientConnectionManager connectionManager) {
        this.connectTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.connectionManager = connectionManager;
    }

    void closeConnection(){
        if (closeableHttpClient != null){
            if(connectionManager == null){
                try {
                    try {
                        closeableHttpClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } finally {

                }
            }
        }
    }

    public HttpClientInternal createNew() {
        return new HttpClientInternal(connectTimeout, readTimeout);
    }

    public <T> HttpResponse execute(HttpMethod httpMethod, HttpRequestDetails requestDetails, T payload, HttpHeaders httpHeaders) {
        initializeClient();
        HttpUriRequest httpRequest;
        httpRequest = initializeRequest(httpMethod, requestDetails);
        requestDetails.addDetailsTo(httpRequest);
        httpHeaders.addTo(httpRequest);
        setRequestBody(payload, httpRequest);
        try {
            return closeableHttpClient.execute(httpRequest);
        } catch (IOException e) {
            throw new WebClientsException("Error executing request", e);
        }
    }

    private <T> void setRequestBody(T payload, HttpUriRequest httpRequest) {

        if (payload == null)
            return;
        if (httpRequest instanceof HttpEntityEnclosingRequestBase) {
            String body;
            try {
                body = objectMapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new WebClientsException("Error serializing payload", e);
            }
            ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        } else {
            throw new WebClientsException(String.format("Request body is not supported in %s request", httpRequest.getMethod()));
        }
    }

    private HttpUriRequest initializeRequest(HttpMethod httpMethod, HttpRequestDetails requestDetails) {
        HttpUriRequest httpRequest;
        switch (httpMethod) {
            case GET:
                httpRequest = new HttpGet(requestDetails.getUri());
                break;
            case POST:
                httpRequest = new HttpPost(requestDetails.getUri());
                break;
            case PUT:
                httpRequest = new HttpPut(requestDetails.getUri());
                break;
            case PATCH:
                httpRequest = new HttpPatch(requestDetails.getUri());
                break;
            default:
                throw new WebClientsException("Unsupported HTTP method for execution with body: " + httpMethod);
        }
        return httpRequest;
    }

    private void initializeClient(){
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(readTimeout)
                .setSocketTimeout(connectTimeout)
                .build();
        if (connectionManager == null) {
            connectionManager = new PoolingHttpClientConnectionManager();
        }
        closeableHttpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }

}
