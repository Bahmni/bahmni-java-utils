package org.bahmni.webclients;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.IOException;

public class HttpClientInternal {
    private int connectTimeout;
    private int readTimeout;
    private DefaultHttpClient defaultHttpClient;


    HttpClientInternal(int connectionTimeout, int readTimeout) {
        this.connectTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public HttpResponse get(HttpRequestDetails requestDetails) {
        return get(requestDetails, new HttpHeaders());
    }

    public HttpResponse get(HttpRequestDetails requestDetails, HttpHeaders httpHeaders) {
        defaultHttpClient = new DefaultHttpClient();
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

    void closeConnection(){
        if (defaultHttpClient != null)
            defaultHttpClient.getConnectionManager().shutdown() ;
    }

    public HttpClientInternal createNew() {
        return new HttpClientInternal(connectTimeout, readTimeout);
    }
}
