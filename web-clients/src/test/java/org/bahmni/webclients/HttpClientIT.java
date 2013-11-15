package org.bahmni.webclients;

import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

@Ignore
public class HttpClientIT {
    @Test
    public void shouldBeAbleToReachOpenMRS() throws URISyntaxException {
        ConnectionDetails connectionDetails = new ConnectionDetails("http://172.18.2.10:8080/openmrs/ws/rest/v1/session", "admin", "test", 10000, 20000);
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));

        System.out.println(httpClient.get(new URI("http://172.18.2.10:8080/openmrs/ws/rest/v1/person/9695b4e2-4449-4cff-becf-963a621ded80")));
    }

}
