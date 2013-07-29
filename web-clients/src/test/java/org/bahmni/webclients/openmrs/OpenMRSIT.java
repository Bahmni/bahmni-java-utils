package org.bahmni.webclients.openmrs;

import org.apache.log4j.Logger;
import org.bahmni.webclients.WebClient;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;

@Ignore
public class OpenMRSIT {
    private static Logger logger = Logger.getLogger(OpenMRSIT.class);

    @Test
    public void testAuthenticate() throws Exception {
        OpenMRSAuthenticator openMRSAuthenticator = new OpenMRSAuthenticator("192.168.33.10:8080", 10000, 20000);
        openMRSAuthenticator.authenticate("admin", "test");
    }

    @Test
    public void get() throws Exception {
        OpenMRSAuthenticator openMRSAuthenticator = new OpenMRSAuthenticator("192.168.33.10:8080", 10000, 20000);
        String jsessionid = openMRSAuthenticator.authenticate("admin", "test");
        WebClient webClient = new WebClient(10000, 20000, "JSESSIONID", jsessionid);
        URI uri = URI.create("http://192.168.33.10:8080/openmrs/ws/rest/v1/patient/0004b3c5-7ca1-4d54-b212-fe63afb8b2da?v=full");
        String response = webClient.get(uri);
        logger.info(response);
    }
}