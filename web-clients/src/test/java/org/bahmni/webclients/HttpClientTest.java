/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients;

import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class HttpClientTest {

    @Mock
    private Authenticator authenticator;

    @Mock
    private HttpClientInternal webClient;

    private URI uri;

    @Before
    public void before() throws URISyntaxException {
        initMocks(this);
        uri = new URI("http://asdf.com");
    }


    @Test
    public void shouldReturnContentFromWebClientResponse() {
        String expectedResponseContent = "good response";
        when(webClient.execute(eq(HttpMethod.GET), any(HttpRequestDetails.class), isNull(), any(HttpHeaders.class)))
                .thenReturn(okResponse(expectedResponseContent));
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);

        String response = authenticatingWebClient.get(uri);

        assertThat(response, containsString(expectedResponseContent));
    }

    @Test
    public void shouldWorkWithoutAnAuthenticator() {
        String expectedResponseContent = "good response";
        when(webClient.execute(eq(HttpMethod.GET), any(HttpRequestDetails.class), isNull(), any(HttpHeaders.class)))
                .thenReturn(okResponse(expectedResponseContent));
        HttpClient authenticatingWebClient = new HttpClient(webClient);

        String response = authenticatingWebClient.get(uri);

        assertThat(response, containsString(expectedResponseContent));
    }

    @Test
    public void shouldRefreshRequestDetailsIfUnauthorizedResponseReceived() {
        String expectedResponseContent = "good response";
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(uri)).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(uri)).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.GET), eq(firstRequestDetails), isNull(), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.execute(eq(HttpMethod.GET), eq(secondRequestDetails), isNull(), any(HttpHeaders.class)))
                .thenReturn(okResponse(expectedResponseContent));
        when(webClient.createNew()).thenReturn(webClient);

        String response = authenticatingWebClient.get(uri);

        verify(webClient).execute(eq(HttpMethod.GET), eq(firstRequestDetails), isNull(), any(HttpHeaders.class));
        verify(webClient).execute(eq(HttpMethod.GET), eq(secondRequestDetails), isNull(), any(HttpHeaders.class));
        assertThat(response, containsString(expectedResponseContent));
    }

    @Test(expected = WebClientsException.class)
    public void shouldFailIfRequestFailsTwiceWithUnauthorizedException() {
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(uri)).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(uri)).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.GET), any(HttpRequestDetails.class), isNull(), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.createNew()).thenReturn(webClient);

        authenticatingWebClient.get(uri);
    }

    private BasicHttpResponse okResponse(String responseContent) {
        BasicHttpResponse basicHttpResponse = basicResponse(HttpStatus.SC_OK, "OK");
        basicHttpResponse.setEntity(new StringEntity(responseContent, ContentType.DEFAULT_TEXT));
        return basicHttpResponse;
    }

    private BasicHttpResponse unAuthorizedResponse() {
        return basicResponse(HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
    }

    private BasicHttpResponse basicResponse(int statusCode, String statusReasonPhrase) {
        return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), statusCode, statusReasonPhrase));
    }

    private BasicHttpResponse forbiddenResponse() {
        return basicResponse(HttpStatus.SC_FORBIDDEN, "Forbidden");
    }

    private BasicHttpResponse serverErrorResponse() {
        return basicResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    }

    // Test classes for JSON serialization/deserialization
    static class TestRequest {
        private String name;
        private int value;

        public TestRequest() {}

        public TestRequest(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    static class TestResponse {
        private String result;
        private boolean success;

        public TestResponse() {}

        public TestResponse(String result, boolean success) {
            this.result = result;
            this.success = success;
        }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    @Test
    public void shouldReturnContentFromPostRequest() throws IOException {
        String responseJson = "{\"result\":\"success\",\"success\":true}";
        TestRequest request = new TestRequest("test", 123);
        
        when(webClient.execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class);

        assertEquals("success", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldSerializePayloadAndDeserializeResponse() throws IOException {
        String responseJson = "{\"result\":\"processed\",\"success\":true}";
        TestRequest request = new TestRequest("testData", 456);
        
        when(webClient.execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class));
        assertEquals("processed", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldRefreshRequestDetailsIfUnauthorizedResponseReceivedOnPost() throws IOException {
        String responseJson = "{\"result\":\"success\",\"success\":true}";
        TestRequest request = new TestRequest("test", 789);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.POST), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.execute(eq(HttpMethod.POST), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.POST), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class));
        verify(webClient).execute(eq(HttpMethod.POST), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class));
        assertEquals("success", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldRefreshRequestDetailsIfForbiddenResponseReceivedOnPost() throws IOException {
        String responseJson = "{\"result\":\"success\",\"success\":true}";
        TestRequest request = new TestRequest("test", 999);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.POST), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(forbiddenResponse());
        when(webClient.execute(eq(HttpMethod.POST), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.POST), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class));
        verify(webClient).execute(eq(HttpMethod.POST), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class));
        assertEquals("success", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test(expected = WebClientsException.class)
    public void shouldFailIfPostRequestFailsTwiceWithUnauthorizedException() throws IOException {
        TestRequest request = new TestRequest("test", 111);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.createNew()).thenReturn(webClient);

        authenticatingWebClient.post(uri.toString(), request, TestResponse.class);
    }

    @Test(expected = WebClientsException.class)
    public void shouldThrowExceptionOnBadResponseCodeForPost() throws IOException {
        TestRequest request = new TestRequest("test", 222);
        
        when(webClient.execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(serverErrorResponse());
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        authenticatingWebClient.post(uri.toString(), request, TestResponse.class);
    }

    @Test
    public void shouldReturnContentFromPutRequest() throws IOException {
        String responseJson = "{\"result\":\"updated\",\"success\":true}";
        TestRequest request = new TestRequest("test", 123);
        
        when(webClient.execute(eq(HttpMethod.PUT), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.put(uri.toString(), request, TestResponse.class);

        assertEquals("updated", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldSerializePayloadAndDeserializeResponseForPut() throws IOException {
        String responseJson = "{\"result\":\"processed\",\"success\":true}";
        TestRequest request = new TestRequest("updateData", 456);
        
        when(webClient.execute(eq(HttpMethod.PUT), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.put(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.PUT), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class));
        assertEquals("processed", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldRefreshRequestDetailsIfUnauthorizedResponseReceivedOnPut() throws IOException {
        String responseJson = "{\"result\":\"updated\",\"success\":true}";
        TestRequest request = new TestRequest("test", 789);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.PUT), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.execute(eq(HttpMethod.PUT), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.put(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.PUT), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class));
        verify(webClient).execute(eq(HttpMethod.PUT), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class));
        assertEquals("updated", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldRefreshRequestDetailsIfForbiddenResponseReceivedOnPut() throws IOException {
        String responseJson = "{\"result\":\"updated\",\"success\":true}";
        TestRequest request = new TestRequest("test", 999);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.PUT), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(forbiddenResponse());
        when(webClient.execute(eq(HttpMethod.PUT), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.put(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.PUT), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class));
        verify(webClient).execute(eq(HttpMethod.PUT), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class));
        assertEquals("updated", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test(expected = WebClientsException.class)
    public void shouldFailIfPutRequestFailsTwiceWithUnauthorizedException() throws IOException {
        TestRequest request = new TestRequest("test", 111);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.PUT), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.createNew()).thenReturn(webClient);

        authenticatingWebClient.put(uri.toString(), request, TestResponse.class);
    }

    @Test(expected = WebClientsException.class)
    public void shouldThrowExceptionOnBadResponseCodeForPut() throws IOException {
        TestRequest request = new TestRequest("test", 222);
        
        when(webClient.execute(eq(HttpMethod.PUT), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(serverErrorResponse());
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        authenticatingWebClient.put(uri.toString(), request, TestResponse.class);
    }

    @Test
    public void shouldReturnContentFromPatchRequest() throws IOException {
        String responseJson = "{\"result\":\"patched\",\"success\":true}";
        TestRequest request = new TestRequest("test", 123);
        
        when(webClient.execute(eq(HttpMethod.PATCH), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.patch(uri.toString(), request, TestResponse.class);

        assertEquals("patched", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldSerializePayloadAndDeserializeResponseForPatch() throws IOException {
        String responseJson = "{\"result\":\"processed\",\"success\":true}";
        TestRequest request = new TestRequest("patchData", 456);
        
        when(webClient.execute(eq(HttpMethod.PATCH), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.patch(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.PATCH), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class));
        assertEquals("processed", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldRefreshRequestDetailsIfUnauthorizedResponseReceivedOnPatch() throws IOException {
        String responseJson = "{\"result\":\"patched\",\"success\":true}";
        TestRequest request = new TestRequest("test", 789);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.PATCH), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.execute(eq(HttpMethod.PATCH), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.patch(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.PATCH), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class));
        verify(webClient).execute(eq(HttpMethod.PATCH), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class));
        assertEquals("patched", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test
    public void shouldRefreshRequestDetailsIfForbiddenResponseReceivedOnPatch() throws IOException {
        String responseJson = "{\"result\":\"patched\",\"success\":true}";
        TestRequest request = new TestRequest("test", 999);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.PATCH), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(forbiddenResponse());
        when(webClient.execute(eq(HttpMethod.PATCH), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.patch(uri.toString(), request, TestResponse.class);

        verify(webClient).execute(eq(HttpMethod.PATCH), eq(firstRequestDetails), any(String.class), any(HttpHeaders.class));
        verify(webClient).execute(eq(HttpMethod.PATCH), eq(secondRequestDetails), any(String.class), any(HttpHeaders.class));
        assertEquals("patched", response.getResult());
        assertEquals(true, response.isSuccess());
    }

    @Test(expected = WebClientsException.class)
    public void shouldFailIfPatchRequestFailsTwiceWithUnauthorizedException() throws IOException {
        TestRequest request = new TestRequest("test", 111);
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(any(URI.class))).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(any(URI.class))).thenReturn(secondRequestDetails);

        when(webClient.execute(eq(HttpMethod.PATCH), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.createNew()).thenReturn(webClient);

        authenticatingWebClient.patch(uri.toString(), request, TestResponse.class);
    }

    @Test(expected = WebClientsException.class)
    public void shouldThrowExceptionOnBadResponseCodeForPatch() throws IOException {
        TestRequest request = new TestRequest("test", 222);
        
        when(webClient.execute(eq(HttpMethod.PATCH), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(serverErrorResponse());
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        authenticatingWebClient.patch(uri.toString(), request, TestResponse.class);
    }

    @Test
    public void shouldUseDefaultHeadersWhenNoCustomHeadersProvided() throws IOException {
        String responseJson = "{\"result\":\"success\",\"success\":true}";
        TestRequest request = new TestRequest("test", 123);
        ArgumentCaptor<HttpHeaders> headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
        
       when(webClient.execute(eq(HttpMethod.GET), any(HttpRequestDetails.class), isNull(), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));

        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
      
        authenticatingWebClient.get(uri.toString(), TestResponse.class);
        verify(webClient).execute(eq(HttpMethod.GET), any(HttpRequestDetails.class), isNull(), headersCaptor.capture());
        HttpHeaders getHeaders = headersCaptor.getValue();
        assertEquals("application/json", getHeaders.get("Accept"));
        
        authenticatingWebClient.post(uri.toString(), request, TestResponse.class);
        verify(webClient).execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), headersCaptor.capture());
        HttpHeaders postHeaders = headersCaptor.getValue();
        assertEquals("application/json", postHeaders.get("Accept"));
        assertEquals("application/json", postHeaders.get("Content-Type"));
    }

    @Test
    public void shouldUseProvidedHttpHeadersForGet() throws IOException {
        String responseJson = "{\"result\":\"fetched\",\"success\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.put("Accept", "application/fhir+json");
        headers.put("X-Custom-Header", "custom-value");
        ArgumentCaptor<HttpHeaders> headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);

        when(webClient.execute(eq(HttpMethod.GET), any(HttpRequestDetails.class), isNull(), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));

        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.get(uri.toString(), TestResponse.class, headers);

        verify(webClient).execute(eq(HttpMethod.GET), any(HttpRequestDetails.class), isNull(), headersCaptor.capture());
        assertEquals("fetched", response.getResult());

        HttpHeaders mergedHeaders = headersCaptor.getValue();
        assertEquals("application/fhir+json", mergedHeaders.get("Accept"));
        assertEquals("custom-value", mergedHeaders.get("X-Custom-Header"));
    }

    @Test
    public void shouldUseProvidedHttpHeadersForPost() throws IOException {
        String responseJson = "{\"result\":\"created\",\"success\":true}";
        TestRequest request = new TestRequest("test", 123);
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/fhir+json");
        headers.put("Accept", "application/fhir+json");
        ArgumentCaptor<HttpHeaders> headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);

        when(webClient.execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));

        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class, headers);

        verify(webClient).execute(eq(HttpMethod.POST), any(HttpRequestDetails.class), any(String.class), headersCaptor.capture());
        assertEquals("created", response.getResult());

        HttpHeaders mergedHeaders = headersCaptor.getValue();
        assertEquals("application/fhir+json", mergedHeaders.get("Content-Type"));
        assertEquals("application/fhir+json", mergedHeaders.get("Accept"));
    }

    @Test
    public void shouldUseProvidedHttpHeadersForPut() throws IOException {
        String responseJson = "{\"result\":\"updated\",\"success\":true}";
        TestRequest request = new TestRequest("test", 123);
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/fhir+json");
        headers.put("Accept", "application/fhir+json");
        ArgumentCaptor<HttpHeaders> headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
        
        when(webClient.execute(eq(HttpMethod.PUT), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.put(uri.toString(), request, TestResponse.class, headers);

        verify(webClient).execute(eq(HttpMethod.PUT), any(HttpRequestDetails.class), any(String.class), headersCaptor.capture());
        assertEquals("updated", response.getResult());

        HttpHeaders mergedHeaders = headersCaptor.getValue();
        assertEquals("application/fhir+json", mergedHeaders.get("Content-Type"));
        assertEquals("application/fhir+json", mergedHeaders.get("Accept"));
    }

    @Test
    public void shouldUseProvidedHttpHeadersForPatch() throws IOException {
        String responseJson = "{\"result\":\"patched\",\"success\":true}";
        TestRequest request = new TestRequest("test", 456);
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json-patch+json");
        headers.put("If-Match", "W/\"1\"");
        ArgumentCaptor<HttpHeaders> headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
        
        when(webClient.execute(eq(HttpMethod.PATCH), any(HttpRequestDetails.class), any(String.class), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.patch(uri.toString(), request, TestResponse.class, headers);

        verify(webClient).execute(eq(HttpMethod.PATCH), any(HttpRequestDetails.class), any(String.class), headersCaptor.capture());
        assertEquals("patched", response.getResult());

        HttpHeaders mergedHeaders = headersCaptor.getValue();
        assertEquals("application/json-patch+json", mergedHeaders.get("Content-Type"));
        assertEquals("application/json", mergedHeaders.get("Accept"));
        assertEquals("W/\"1\"", mergedHeaders.get("If-Match"));
    }
}
