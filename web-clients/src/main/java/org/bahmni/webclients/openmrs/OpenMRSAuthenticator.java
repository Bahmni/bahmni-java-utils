package org.bahmni.webclients.openmrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.webclients.WebClientsException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class OpenMRSAuthenticator {
    private static Logger logger = LoggerFactory.getLogger(OpenMRSAuthenticator.class);
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
     * @param user in plain text
     * @param password in plain text
     * @param objectMapper
     * @return JSessionId value
     */
    public OpenMRSAuthenticationResponse authenticate(String user, String password, ObjectMapper objectMapper) {
        String responseText = null;

        HttpGet httpGet = new HttpGet(authURL);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(readTimeout)
                .setConnectionRequestTimeout(readTimeout)
                .build();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();

        try {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, credentials);
            HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(new BasicCookieStore());
            context.setCredentialsProvider(credsProvider);
            Header authorizationHeader = new BasicScheme(StandardCharsets.UTF_8).authenticate(credentials , httpGet, context);
            httpGet.setHeader(authorizationHeader);

            logger.info(String.format("Executing request: %s", httpGet.getRequestLine()));

            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream content = entity.getContent();
                responseText = IOUtils.toString(content);
            }
            logger.info("OMRS Authentication successful");
            logger.debug(String.format("Authentication response: %s", responseText));
            EntityUtils.consume(entity);

            return objectMapper.readValue(responseText, OpenMRSAuthenticationResponse.class);
        } catch (Exception e) {
            throw new WebClientsException(e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}