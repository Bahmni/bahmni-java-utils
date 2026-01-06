package org.bahmni.webclients;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;

import static org.bahmni.webclients.ObjectMapperRepository.objectMapper;

public class HttpClient {
    private Authenticator authenticator;
    private HttpClientInternal httpClientInternal;


    public HttpClient(ConnectionDetails connectionDetails) {
        this(new HttpClientInternal(connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout()));
    }

    public HttpClient(ConnectionDetails connectionDetails, Authenticator authenticator) {
        this(new HttpClientInternal(connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout(), connectionDetails.getConnectionManager()), authenticator);
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
        return executeWithAuthRetry(HttpMethod.GET, uri, null, new HttpHeaders());
    }

    public <T> T get(String url, Class<T> returnType) throws IOException {
        HttpHeaders httpHeaders = getDefaultHeaders();
        String response = executeWithAuthRetry(HttpMethod.GET, URI.create(url), null, httpHeaders);
        return objectMapper.readValue(response, returnType);
    }

    public <T, R> R post(String url, T payload, Class<R> returnType) throws IOException {
        HttpHeaders httpHeaders = getPostPutPatchDefaultHeaders();
        String response = executeWithAuthRetry(HttpMethod.POST, URI.create(url), payload, httpHeaders);
        return objectMapper.readValue(response, returnType);

    }

    public <T, R> R put(String url, T payload, Class<R> returnType) throws IOException {
        HttpHeaders httpHeaders = getPostPutPatchDefaultHeaders();
        return put(url, payload, returnType, httpHeaders);
    }

    public <T, R> R put(String url, T payload, Class<R> returnType, HttpHeaders httpHeaders) throws IOException {
        httpHeaders = getMergedHeaders(getPostPutPatchDefaultHeaders(), httpHeaders);
        String response = executeWithAuthRetry(HttpMethod.PUT, URI.create(url), payload, httpHeaders);
        return objectMapper.readValue(response, returnType);
    }

    public <T, R> R patch(String url, T payload, Class<R> returnType) throws IOException {
        HttpHeaders httpHeaders = getPostPutPatchDefaultHeaders();
        return patch(url, payload, returnType, httpHeaders);
    }

    public <T, R> R patch(String url, T payload, Class<R> returnType, HttpHeaders httpHeaders) throws IOException {
        httpHeaders = getMergedHeaders(getPostPutPatchDefaultHeaders(), httpHeaders);
        String response = executeWithAuthRetry(HttpMethod.PATCH, URI.create(url), payload, httpHeaders);
        return objectMapper.readValue(response, returnType);
    }

    private <T> String executeWithAuthRetry(HttpMethod httpMethod, URI uri, T payload, HttpHeaders httpHeaders) {
        try {
            HttpResponse httpResponse = httpClientInternal.execute(httpMethod, authenticator.getRequestDetails(uri), payload, httpHeaders);

            if (isAuthFailure(httpResponse)) {
                reInitializeClient();
                httpResponse = httpClientInternal.execute(httpMethod, authenticator.refreshRequestDetails(uri), payload, httpHeaders);
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
        HttpEntity entity = httpResponse.getEntity();
        if (statusCode < 200 || statusCode >= 300) {
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                throw new WebClientsException("Unable to read response entity", e);
            }
            throw new WebClientsException("Bad response code of " + statusCode);
        }

        if (entity == null) throw new WebClientsException("Cannot read response");
    }

    private String asString(HttpResponse httpResponse) throws IOException {
        return EntityUtils.toString(httpResponse.getEntity());
    }

    private boolean isAuthFailure(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED ||
                httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN;
    }

    private void reInitializeClient() {
        httpClientInternal.closeConnection();
        httpClientInternal = httpClientInternal.createNew();
    }

    private HttpHeaders getDefaultHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("Accept", "application/json");
        return httpHeaders;
    }

    private HttpHeaders getPostPutPatchDefaultHeaders() {
        HttpHeaders httpHeaders = getDefaultHeaders();
        httpHeaders.put("Content-Type", "application/json");
        return httpHeaders;
    }

    private HttpHeaders getMergedHeaders(HttpHeaders base, HttpHeaders custom) {
        HttpHeaders merged = new HttpHeaders();
        if (base != null) merged.putAll(base);
        if (custom != null) merged.putAll(custom);
        return merged;
    }
}
