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

import java.net.URI;
import java.net.URISyntaxException;

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
}
