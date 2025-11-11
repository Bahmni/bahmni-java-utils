package org.bahmni.webclients;

import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
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
        when(webClient.get(any(HttpRequestDetails.class), any(HttpHeaders.class))).thenReturn(okResponse(expectedResponseContent));
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);

        String response = authenticatingWebClient.get(uri);

        assertThat(response, containsString(expectedResponseContent));
    }

    @Test
    public void shouldWorkWithoutAnAuthenticator() {
        String expectedResponseContent = "good response";
        when(webClient.get(any(HttpRequestDetails.class), any(HttpHeaders.class))).thenReturn(okResponse(expectedResponseContent));
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

        when(webClient.get(eq(firstRequestDetails), any(HttpHeaders.class))).thenReturn(unAuthorizedResponse());
        when(webClient.get(eq(secondRequestDetails), any(HttpHeaders.class))).thenReturn(okResponse(expectedResponseContent));
        when(webClient.createNew()).thenReturn(webClient);

        String response = authenticatingWebClient.get(uri);

        verify(webClient).get(eq(firstRequestDetails), any(HttpHeaders.class));
        verify(webClient).get(eq(secondRequestDetails), any(HttpHeaders.class));
        assertThat(response, containsString(expectedResponseContent));
    }

    @Test(expected = WebClientsException.class)
    public void shouldFailIfRequestFailsTwiceWithUnauthorizedException() {
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        HttpRequestDetails firstRequestDetails = new HttpRequestDetails(uri);
        HttpRequestDetails secondRequestDetails = new HttpRequestDetails(uri);

        when(authenticator.getRequestDetails(uri)).thenReturn(firstRequestDetails);
        when(authenticator.refreshRequestDetails(uri)).thenReturn(secondRequestDetails);

        when(webClient.get(any(HttpRequestDetails.class), any(HttpHeaders.class))).thenReturn(unAuthorizedResponse());
        when(webClient.get(any(HttpRequestDetails.class), any(HttpHeaders.class))).thenReturn(unAuthorizedResponse());
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
        String requestJson = "{\"name\":\"test\",\"value\":123}";
        String responseJson = "{\"result\":\"success\",\"success\":true}";
        TestRequest request = new TestRequest("test", 123);
        
        when(webClient.post(any(HttpRequestDetails.class), anyString(), any(HttpHeaders.class)))
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
        
        when(webClient.post(any(HttpRequestDetails.class), anyString(), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class);

        verify(webClient).post(any(HttpRequestDetails.class), contains("testData"), any(HttpHeaders.class));
        verify(webClient).post(any(HttpRequestDetails.class), contains("456"), any(HttpHeaders.class));
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

        when(webClient.post(eq(firstRequestDetails), anyString(), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.post(eq(secondRequestDetails), anyString(), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class);

        verify(webClient).post(eq(firstRequestDetails), anyString(), any(HttpHeaders.class));
        verify(webClient).post(eq(secondRequestDetails), anyString(), any(HttpHeaders.class));
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

        when(webClient.post(eq(firstRequestDetails), anyString(), any(HttpHeaders.class)))
                .thenReturn(forbiddenResponse());
        when(webClient.post(eq(secondRequestDetails), anyString(), any(HttpHeaders.class)))
                .thenReturn(okResponse(responseJson));
        when(webClient.createNew()).thenReturn(webClient);

        TestResponse response = authenticatingWebClient.post(uri.toString(), request, TestResponse.class);

        verify(webClient).post(eq(firstRequestDetails), anyString(), any(HttpHeaders.class));
        verify(webClient).post(eq(secondRequestDetails), anyString(), any(HttpHeaders.class));
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

        when(webClient.post(any(HttpRequestDetails.class), anyString(), any(HttpHeaders.class)))
                .thenReturn(unAuthorizedResponse());
        when(webClient.createNew()).thenReturn(webClient);

        authenticatingWebClient.post(uri.toString(), request, TestResponse.class);
    }

    @Test(expected = WebClientsException.class)
    public void shouldThrowExceptionOnBadResponseCodeForPost() throws IOException {
        TestRequest request = new TestRequest("test", 222);
        
        when(webClient.post(any(HttpRequestDetails.class), anyString(), any(HttpHeaders.class)))
                .thenReturn(serverErrorResponse());
        
        HttpClient authenticatingWebClient = new HttpClient(webClient, authenticator);
        authenticatingWebClient.post(uri.toString(), request, TestResponse.class);
    }
}
