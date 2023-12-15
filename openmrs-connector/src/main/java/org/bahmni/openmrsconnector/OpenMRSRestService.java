package org.bahmni.openmrsconnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.openmrsconnector.response.AuthenticationResponse;
import org.bahmni.openmrsconnector.response.Resource;
import org.bahmni.openmrsconnector.response.ResourceResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class OpenMRSRestService {
    private RestTemplate restTemplate = new RestTemplate();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Log log = LogFactory.getLog(OpenMRSRestService.class);
    private String sessionId;
    private static Logger logger = LoggerFactory.getLogger(OpenMRSRestService.class);
    private AllPatientAttributeTypes allPatientAttributeTypes;
    private OpenMRSRESTConnection openMRSRESTConnection;
    private Map<String, String> allEncounterTypes;
    private Map<String, String> allVisitTypes;

    public OpenMRSRestService(OpenMRSRESTConnection openMRSRESTConnection) throws IOException, URISyntaxException {
        this.openMRSRESTConnection = openMRSRESTConnection;
        authenticate();
        loadReferences();
    }

    public void authenticate() throws URISyntaxException, IOException {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Basic " + openMRSRESTConnection.encodedLogin());
        HttpEntity requestEntity = new HttpEntity<MultiValueMap>(new LinkedMultiValueMap<String, String>(), requestHeaders);
        String authURL = openMRSRESTConnection.getRestApiUrl() + "session";
        ResponseEntity<String> exchange = restTemplate.exchange(new URI(authURL), HttpMethod.GET, requestEntity, String.class);
        logger.info(exchange.getBody());
        AuthenticationResponse authenticationResponse = objectMapper.readValue(exchange.getBody(), AuthenticationResponse.class);
        sessionId = authenticationResponse.getSessionId();
    }

    private void loadReferences() throws URISyntaxException, IOException {
        loadAllPatientAttributes();
        loadAllEncounterTypes();
        loadAllVisitTypes();
    }

    private void loadAllPatientAttributes() throws URISyntaxException, IOException {
        allPatientAttributeTypes = new AllPatientAttributeTypes();
        String jsonResponse = executeHTTPMethod("personattributetype?v=full", HttpMethod.GET);
        ResourceResponse resourceResponse = objectMapper.readValue(jsonResponse, ResourceResponse.class);
        for (Resource resource : resourceResponse.getResults())
            allPatientAttributeTypes.addPersonAttributeType(resource.getName(), resource.getUuid());
    }

    public AllPatientAttributeTypes getAllPatientAttributeTypes() {
        return allPatientAttributeTypes;
    }

    public Map<String, String> getAllEncounterTypes() throws URISyntaxException, IOException {
        return allEncounterTypes;
    }

    public Map<String, String> getAllVisitTypes() throws URISyntaxException, IOException {
        return allVisitTypes;
    }

    private void loadAllEncounterTypes() throws URISyntaxException, IOException {
        allEncounterTypes = new HashMap<>();
        String jsonResponse = executeHTTPMethod("encountertype?v=full", HttpMethod.GET);
        ResourceResponse encounterTypes = objectMapper.readValue(jsonResponse, ResourceResponse.class);
        for ( Resource encounterType : encounterTypes.getResults()){
            allEncounterTypes.put(encounterType.getName(), encounterType.getUuid());
        }
    }

    private void loadAllVisitTypes() throws URISyntaxException, IOException {
        allVisitTypes = new HashMap<>();
        String jsonResponse = executeHTTPMethod("visittype?v=full", HttpMethod.GET);
        ResourceResponse visitTypes = objectMapper.readValue(jsonResponse, ResourceResponse.class);
        for ( Resource visitType : visitTypes.getResults()){
            allVisitTypes.put(visitType.getName(), visitType.getUuid());
        }
    }

    private String executeHTTPMethod(String urlSuffix, HttpMethod method) throws URISyntaxException {
        HttpHeaders requestHeaders = getHttpHeaders();
        String referencesURL = openMRSRESTConnection.getRestApiUrl() + urlSuffix;
        HttpEntity requestEntity = new HttpEntity<MultiValueMap>(new LinkedMultiValueMap<String, String>(), requestHeaders);
        ResponseEntity<String> exchange = restTemplate.exchange(new URI(referencesURL), method, requestEntity, String.class);
        logger.debug("({}) - {}", urlSuffix, exchange.getBody());
        return exchange.getBody();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Cookie", "JSESSIONID=" + sessionId);
        return requestHeaders;
    }

    public String getSessionId() {
        return sessionId;
    }
}