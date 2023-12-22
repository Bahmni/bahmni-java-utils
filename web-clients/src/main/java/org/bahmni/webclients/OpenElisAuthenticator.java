package org.bahmni.webclients;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenElisAuthenticator implements Authenticator {
    private static Logger logger = LoggerFactory.getLogger(OpenElisAuthenticator.class);
    private final ConnectionDetails authenticationDetails;
    private HttpRequestDetails previousSuccessfulRequest;
    CloseableHttpClient httpClient;

    public OpenElisAuthenticator(ConnectionDetails authenticationDetails) {
        this.authenticationDetails = authenticationDetails;
    }

    @Override
    public HttpRequestDetails getRequestDetails(URI uri) {
        if (previousSuccessfulRequest == null || previousSuccessfulRequest.getClientCookies().size() == 0) {
            return refreshRequestDetails(uri);
        }
        return previousSuccessfulRequest.createNewWith(uri);
    }

    @Override
    public HttpRequestDetails refreshRequestDetails(URI uri) {
        try {

            CookieStore cookieStore = new BasicCookieStore();
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            HttpPost httpPost = new HttpPost(uri);

            logger.info(String.format("Executing request: %s", httpPost.getRequestLine()));
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(authenticationDetails.getConnectionTimeout())
                    .setSocketTimeout(authenticationDetails.getReadTimeout())
                    .build();
            httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .build();

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("loginName", authenticationDetails.getUserId()));
            params.add(new BasicNameValuePair("password", authenticationDetails.getPassword()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpClient.execute(httpPost, httpContext);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new WebClientsException("Authentication with OpenELIS failed");
            }

            ClientCookies clientCookies = new ClientCookies();
            List<Cookie> cookies = cookieStore.getCookies();
            for (Cookie cookie : cookies) {
                String SESSION_ID_KEY = "JSESSIONID";
                if (cookie.getName().equalsIgnoreCase(SESSION_ID_KEY)) {
                    clientCookies.put(SESSION_ID_KEY, cookie.getValue());
                }
            }

            previousSuccessfulRequest = new HttpRequestDetails(uri, clientCookies, new HttpHeaders());
            return previousSuccessfulRequest;

        } catch (Exception e) {
            throw new WebClientsException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
