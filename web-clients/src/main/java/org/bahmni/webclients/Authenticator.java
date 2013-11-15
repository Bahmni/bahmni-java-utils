package org.bahmni.webclients;

import java.net.URI;

public interface Authenticator {
    public HttpRequestDetails getRequestDetails(URI uri);

    public HttpRequestDetails refreshRequestDetails(URI uri);
}
