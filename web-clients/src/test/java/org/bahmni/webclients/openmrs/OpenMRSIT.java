package org.bahmni.webclients.openmrs;

import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.webclients.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Ignored because without mocking we can't really write the tests like below.
 * Alt 1. have a known available server, setup data programmatically calling REST apis
 */
@Ignore
public class OpenMRSIT {
    private static final String BAHMNI_SERVER_BASE_URL = "https://qa-02.hip.bahmni-covid19.in";
    private static final String PATIENT_UUID = "0004b3c5-7ca1-4d54-b212-fe63afb8b2da";
    private static Logger logger = LoggerFactory.getLogger(OpenMRSIT.class);
    private OpenMRSAuthenticator openMRSAuthenticator;
    private String authURL;

    @Before
    public void before() {
        authURL = BAHMNI_SERVER_BASE_URL + "/openmrs/ws/rest/v1/session";
        openMRSAuthenticator = new OpenMRSAuthenticator(authURL, 10000, 20000);
    }

    @Test
    public void successfulAuth() throws Exception {
        OpenMRSAuthenticationResponse authenticationResponse = openMRSAuthenticator.authenticate("admin", "test", ObjectMapperRepository.objectMapper);
        Assert.assertTrue(authenticationResponse.isAuthenticated());
    }

    @Test
    public void unSuccessfulAuth() throws Exception {
        OpenMRSAuthenticationResponse authenticationResponse = openMRSAuthenticator.authenticate("admin", "test1", ObjectMapperRepository.objectMapper);
        Assert.assertFalse(authenticationResponse.isAuthenticated());
    }

    @Test
    @Ignore
    //Need to fix the mock
    public void goodResponse() throws Exception {
        URI uri = URI.create(BAHMNI_SERVER_BASE_URL + "/openmrs/ws/rest/v1/patient/" + PATIENT_UUID + "?v=full");

        BasicHttpResponse goodResponse = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(1, 1), 200, ""));
        goodResponse.setEntity(getGoodResponseData());

        HttpClientInternal mockHttpClientInternal = mock(HttpClientInternal.class);
        when(mockHttpClientInternal.get(any(HttpRequestDetails.class))).thenReturn(goodResponse);

        HttpRequestDetails goodRequestWithAuthenticationDetails = new HttpRequestDetails(uri);
        Authenticator mockAuthenticator = mock(Authenticator.class);
        when(mockAuthenticator.getRequestDetails(uri)).thenReturn(goodRequestWithAuthenticationDetails);

        HttpClient webClient = new HttpClient(mockHttpClientInternal, mockAuthenticator);

        String webResponse = webClient.get(uri);
        logger.info(webResponse);
    }


    private BasicHttpEntity getGoodResponseData() {
        String responseData = "";
        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
        basicHttpEntity.setContent(new ByteArrayInputStream(responseData.getBytes()));
        return basicHttpEntity;
    }
}
