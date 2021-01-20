package org.bahmni.webclients;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;

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


    public HttpResponse get(HttpRequestDetails requestDetails) {
        return get(requestDetails, new HttpHeaders());
    }

    public HttpResponse get(HttpRequestDetails requestDetails, HttpHeaders httpHeaders) {

        HttpGet httpGet = new HttpGet(requestDetails.getUri());
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(readTimeout)
                .setSocketTimeout(connectTimeout)
                .build();
        if(connectionManager == null) {
            connectionManager = new PoolingHttpClientConnectionManager();
        }
        closeableHttpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
        requestDetails.addDetailsTo(httpGet);
        httpHeaders.addTo(httpGet);

        try {
            return closeableHttpClient.execute(httpGet);
        } catch (IOException e) {
            throw new WebClientsException("Error executing request", e);
        }
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
}
