package org.bahmni.webclients.openmrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.webclients.WebClientsException;

import java.io.InputStream;

public class OpenMRSAuthenticator {
    private static Logger logger = LogManager.getLogger(OpenMRSAuthenticator.class);
    private String authURL;
    private int connectionTimeout;
    private int readTimeout;

    public OpenMRSAuthenticator(String authURL, int connectionTimeout, int readTimeout) {
        this.authURL = authURL;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     *
     * @param user     in plain text
     * @param password in plain text
     * @return JSessionId value
     * @throws AuthenticationException, IOException
     */
    public OpenMRSAuthenticationResponse authenticate(String user, String password, ObjectMapper objectMapper) {
        String responseText = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);

            HttpGet httpGet = new HttpGet(authURL);

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

            return objectMapper.readValue(responseText, OpenMRSAuthenticationResponse.class);
        } catch (Exception e) {
            throw new WebClientsException(e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}