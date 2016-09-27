package org.bahmni.webclients;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;

import java.io.IOException;

public class HttpClientInternal {
    private int connectTimeout;
    private int readTimeout;
    private DefaultHttpClient defaultHttpClient;
    private ClientConnectionManager connectionManager;

    HttpClientInternal(int connectionTimeout, int readTimeout) {
        this.connectTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    HttpClientInternal(int connectionTimeout, int readTimeout, ClientConnectionManager connectionManager) {
        this.connectTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.connectionManager = connectionManager;
    }


    public HttpResponse get(HttpRequestDetails requestDetails) {
        return get(requestDetails, new HttpHeaders());
    }

    public HttpResponse get(HttpRequestDetails requestDetails, HttpHeaders httpHeaders) {
        defaultHttpClient = (connectionManager == null) ? new DefaultHttpClient() : new DefaultHttpClient(new PoolingClientConnectionManager());
        defaultHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);
        defaultHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);

        HttpGet httpGet = new HttpGet(requestDetails.getUri());
        requestDetails.addDetailsTo(httpGet);
        httpHeaders.addTo(httpGet);

        try {
            return defaultHttpClient.execute(httpGet);
        } catch (IOException e) {
            throw new WebClientsException("Error executing request", e);
        }
    }

    void closeConnection() {
        if (defaultHttpClient != null){
            if(connectionManager == null){
                defaultHttpClient.getConnectionManager().shutdown();
            }else{
                defaultHttpClient.getConnectionManager().closeExpiredConnections();
            }
        }
    }

    public HttpClientInternal createNew() {
        return new HttpClientInternal(connectTimeout, readTimeout);
    }
}
