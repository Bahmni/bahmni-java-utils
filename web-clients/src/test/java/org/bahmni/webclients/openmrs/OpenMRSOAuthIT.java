/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.HttpRequestDetails;
import org.bahmni.webclients.OAuth2ConnectionDetails;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Runs against a real Keycloak + OpenMRS; skipped unless all of these system properties are set:
 * -Dit.openmrs.session.url=https://localhost/openmrs/ws/rest/v1/session
 * -Dit.oauth.token.url=https://localhost/keycloak/realms/mihealth/protocol/openid-connect/token
 * -Dit.oauth.client.id=odoo-connect-sa -Dit.oauth.client.secret=...
 * For a self-signed proxy certificate also pass -Djavax.net.ssl.trustStore=... -Djavax.net.ssl.trustStorePassword=...
 */
public class OpenMRSOAuthIT {
    private String sessionUrl;
    private Authenticator authenticator;

    @Before
    public void setUp() {
        sessionUrl = System.getProperty("it.openmrs.session.url");
        String tokenUrl = System.getProperty("it.oauth.token.url");
        String clientId = System.getProperty("it.oauth.client.id");
        String clientSecret = System.getProperty("it.oauth.client.secret");
        Assume.assumeTrue(sessionUrl != null && tokenUrl != null && clientId != null && clientSecret != null);

        OAuth2ConnectionDetails credentials = new OAuth2ConnectionDetails(sessionUrl, tokenUrl, clientId, clientSecret,
                null, 10000, 20000, OAuth2ConnectionDetails.DEFAULT_EXPIRY_SKEW_SECONDS);
        authenticator = new OpenMRSOAuthLoginAuthenticator(credentials);
    }

    @Test
    public void bearerLoginYieldsSessionThatWorksForRestCalls() {
        HttpRequestDetails details = authenticator.getRequestDetails(URI.create(restUrl("location?limit=1")));
        assertNotNull(details.getClientCookies().get("JSESSIONID"));

        String body = client().get(URI.create(restUrl("location?limit=1")));
        assertTrue(body, body.contains("results"));
    }

    @Test
    public void sessionAlsoWorksForAtomfeed() {
        String body = client().get(URI.create(sessionUrl.replace("/rest/v1/session", "/atomfeed/encounter/recent")));
        assertTrue(body, body.contains("<feed"));
    }

    @Test
    public void refreshAfterSessionLossStillWorks() {
        URI uri = URI.create(restUrl("location?limit=1"));
        authenticator.getRequestDetails(uri);

        HttpRequestDetails refreshed = authenticator.refreshRequestDetails(uri);

        assertNotNull(refreshed.getClientCookies().get("JSESSIONID"));
    }

    private HttpClient client() {
        return new HttpClient(new ConnectionDetails(sessionUrl, "", "", 10000, 20000), authenticator);
    }

    private String restUrl(String path) {
        return sessionUrl.replace("/session", "/" + path);
    }
}
