package org.bahmni.webclients;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

public class HttpClient {
    private Authenticator authenticator;
    private HttpClientInternal httpClientInternal;


    public HttpClient(ConnectionDetails connectionDetails) {
        this(new HttpClientInternal(connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout()));
    }

    public HttpClient(ConnectionDetails connectionDetails, Authenticator authenticator) {
        this(new HttpClientInternal(connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout()), authenticator);
    }

    //Just for tests
    public HttpClient(HttpClientInternal httpClientInternal) {
        this(httpClientInternal, new NullAuthenticator());
    }

    //Just for tests
    public HttpClient(HttpClientInternal httpClientInternal, Authenticator authenticator) {
        this.httpClientInternal = httpClientInternal;
        this.authenticator = authenticator;
    }

    public ClientCookies getCookies(URI uri) {
        return authenticator.getRequestDetails(uri).getClientCookies();
    }

    public String get(URI uri) {
        try {
            HttpResponse httpResponse = httpClientInternal.get(authenticator.getRequestDetails(uri));

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                httpClientInternal.closeConnection();
                httpClientInternal = httpClientInternal.createNew();
                httpResponse = httpClientInternal.get(authenticator.refreshRequestDetails(uri));
            }

            checkSanityOfResponse(httpResponse);
            return asString(httpResponse);
        } catch (IOException e) {
            throw new WebClientsException(e);
        } finally {
            httpClientInternal.closeConnection();
        }
    }

    private void checkSanityOfResponse(HttpResponse httpResponse) {
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode < 200 || statusCode >= 300) throw new WebClientsException("Bad response code of " + statusCode);

        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) throw new WebClientsException("Cannot read response");
    }

    private String asString(HttpResponse httpResponse) throws IOException {
        InputStream content = httpResponse.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append(System.getProperty("line.separator"));
        }

        return stringBuilder.toString();
    }
}
