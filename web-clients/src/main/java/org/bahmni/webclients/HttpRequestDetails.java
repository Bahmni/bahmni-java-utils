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
