package org.bahmni.webclients;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

public class AnonymousAuthenticator implements Authenticator {
    private static Logger logger = LoggerFactory.getLogger(AnonymousAuthenticator.class);
    private final String SESSION_ID_KEY = "JSESSIONID";
    private final ConnectionDetails authenticationDetails;
    private HttpRequestDetails previousSuccessfulRequest;

    public AnonymousAuthenticator(ConnectionDetails authenticationDetails) {
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
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            CookieStore cookieStore = new BasicCookieStore();
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE,cookieStore);

            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, authenticationDetails.getReadTimeout());
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, authenticationDetails.getConnectionTimeout());

            HttpGet httpGet = new HttpGet(uri);

            logger.info(String.format("Executing request: %s", httpGet.getRequestLine()));

            ClientCookies clientCookies = new ClientCookies();
            HttpResponse response = httpClient.execute(httpGet, httpContext);
            List<Cookie> cookies = cookieStore.getCookies();
            for (Cookie cookie : cookies) {
                if(cookie.getName().equalsIgnoreCase(SESSION_ID_KEY)){
                    clientCookies.put(SESSION_ID_KEY,cookie.getValue());
                }
            }

            previousSuccessfulRequest = new HttpRequestDetails(uri, clientCookies, new HttpHeaders());
            return previousSuccessfulRequest;

        } catch (Exception e) {
            throw new WebClientsException(e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}
