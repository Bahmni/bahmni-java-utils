package org.bahmni.webclients;

import java.net.URI;

class NullAuthenticator implements Authenticator{
    @Override
    public HttpRequestDetails getRequestDetails(URI uri) {
        return new HttpRequestDetails(uri);
    }

    @Override
    public HttpRequestDetails refreshRequestDetails(URI uri) {
        return new HttpRequestDetails(uri);
    }
}
