package org.bahmni.webclients.openmrs;

import org.apache.log4j.Logger;
import org.bahmni.webclients.WebClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;

@Ignore
public class OpenMRSIT {
    private static Logger logger = Logger.getLogger(OpenMRSIT.class);
    private OpenMRSAuthenticator openMRSAuthenticator;

    @Before
    public void before() {
        openMRSAuthenticator = new OpenMRSAuthenticator("http://192.168.33.10:8080/openmrs/ws/rest/v1/session", 10000, 20000);
    }

    @Test
    public void successfulAuth() throws Exception {
        OpenMRSAuthenticationResponse authenticationResponse = openMRSAuthenticator.authenticate("admin", "test", ObjectMapperRepository.OBJECT_MAPPER);
        Assert.assertTrue(authenticationResponse.isAuthenticated());
    }

    @Test
    public void unSuccessfulAuth() throws Exception {
        OpenMRSAuthenticationResponse authenticationResponse = openMRSAuthenticator.authenticate("admin", "test1", ObjectMapperRepository.OBJECT_MAPPER);
        Assert.assertFalse(authenticationResponse.isAuthenticated());
    }

    @Test
    public void get() throws Exception {
        OpenMRSAuthenticationResponse authenticationResponse = openMRSAuthenticator.authenticate("admin", "test", ObjectMapperRepository.OBJECT_MAPPER);
        WebClient webClient = new WebClient(10000, 20000, "JSESSIONID", authenticationResponse.getSessionId());
        URI uri = URI.create("http://192.168.33.10:8080/openmrs/ws/rest/v1/patient/0004b3c5-7ca1-4d54-b212-fe63afb8b2da?v=full");
        String response = webClient.get(uri);
        logger.info(response);
    }
}