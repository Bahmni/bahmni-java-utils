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
    private WebClient webClient;

    public HttpClient(ConnectionDetails connectionDetails) {
        this(new WebClient(connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout()));
    }

    public HttpClient(ConnectionDetails connectionDetails, Authenticator authenticator) {
        this(new WebClient(connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout()), authenticator);
    }

    //Just for tests
    public HttpClient(WebClient webClient) {
        this(webClient, new NullAuthenticator());
    }

    //Just for tests
    public HttpClient(WebClient webClient, Authenticator authenticator) {
        this.authenticator = authenticator;
        this.webClient = webClient;
    }

    public String get(URI uri){
        HttpResponse httpResponse = webClient.get(authenticator.getRequestDetails(uri));
        httpResponse = retryIfRequired(uri, httpResponse);
        checkSanityOfResponse(httpResponse);

        try {
            return asString(httpResponse);
        } catch (IOException e) {
            throw new WebClientsException(e);
        }
    }

    private void checkSanityOfResponse(HttpResponse httpResponse) {
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode < 200 || statusCode >= 300) throw new WebClientsException("Bad response code of " + statusCode);

        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) throw new WebClientsException("Cannot read response");
    }

    private HttpResponse retryIfRequired(URI uri, HttpResponse httpResponse) {
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            httpResponse = webClient.get(authenticator.refreshRequestDetails(uri));
        }
        return httpResponse;
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
