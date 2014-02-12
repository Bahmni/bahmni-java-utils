package org.bahmni.webclients;

import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

//@Ignore
public class HttpClientIT {
    @Test
    public void shouldBeAbleToReachOpenMRS() throws URISyntaxException {
        ConnectionDetails connectionDetails = new ConnectionDetails("http://172.18.2.10:8080/openmrs/ws/rest/v1/session", "admin", "test", 10000, 20000);
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));

        System.out.println(httpClient.get(new URI("http://172.18.2.10:8080/openmrs/ws/rest/v1/person/819f1fb0-c79a-11e2-b284-107d46e7b2c5?v=full")));
    }

}
