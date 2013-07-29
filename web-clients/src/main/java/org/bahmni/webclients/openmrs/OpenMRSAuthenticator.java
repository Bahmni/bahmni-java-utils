package org.bahmni.webclients.openmrs;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;

public class OpenMRSAuthenticator {
    private static Logger logger = Logger.getLogger(OpenMRSAuthenticator.class);
    private String host;
    private int connectionTimeout;
    private int readTimeout;

    public OpenMRSAuthenticator(String host, int connectionTimeout, int readTimeout) {
        this.host = host;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public String authenticate(String user, String password) throws Exception {
        String URL = String.format("http://%s/openmrs/ws/rest/v1/session", host);
        String responseText = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);

            HttpGet httpGet = new HttpGet(URL);

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpGet);
            httpGet.setHeader(authorizationHeader);

            logger.info(String.format("Executing request: %s", httpGet.getRequestLine()));

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream content = entity.getContent();
                responseText = IOUtils.toString(content);
            }
            logger.info(String.format("Authentication response: %s", responseText));
            EntityUtils.consume(entity);
            String sessionId = responseText.substring(14);
            sessionId = sessionId.substring(0, sessionId.indexOf("\""));
            return sessionId;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}