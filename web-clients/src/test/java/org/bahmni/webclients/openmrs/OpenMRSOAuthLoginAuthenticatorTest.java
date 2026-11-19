/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import org.bahmni.webclients.HttpRequestDetails;
import org.bahmni.webclients.WebClientsException;
import org.bahmni.webclients.OAuth2ConnectionDetails;
import org.bahmni.webclients.oauth.StubAuthServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OpenMRSOAuthLoginAuthenticatorTest {
    private static final URI TARGET = URI.create("http://openmrs.test/openmrs/ws/atomfeed/encounter/recent");

    private StubAuthServer server;
    private OpenMRSOAuthLoginAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        server = new StubAuthServer();
        OAuth2ConnectionDetails credentials = new OAuth2ConnectionDetails(server.sessionUrl(), server.tokenUrl(),
                "odoo-connect-sa", "secret", null, 2000, 5000, 30);
        authenticator = new OpenMRSOAuthLoginAuthenticator(credentials);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void logsInWithBearerTokenAndReturnsSessionCookie() {
        server.acceptedBearer = "token-1";

        HttpRequestDetails details = authenticator.getRequestDetails(TARGET);

        assertEquals("Bearer token-1", server.sessionAuthHeaders.get(0));
        assertEquals(StubAuthServer.SESSION_ID, details.getClientCookies().get("JSESSIONID"));
        assertEquals(TARGET, details.getUri());
    }

    @Test
    public void reusesSessionCookieWithoutNewTokenOrLoginCalls() {
        server.acceptedBearer = "token-1";

        authenticator.getRequestDetails(TARGET);
        HttpRequestDetails second = authenticator.getRequestDetails(URI.create("http://openmrs.test/other"));

        assertEquals(1, server.tokenRequests.get());
        assertEquals(1, server.sessionRequests.get());
        assertEquals(StubAuthServer.SESSION_ID, second.getClientCookies().get("JSESSIONID"));
    }

    @Test
    public void refreshAfterEstablishedSessionFetchesFreshToken() {
        server.acceptedBearer = "token-1";
        authenticator.getRequestDetails(TARGET);

        server.acceptedBearer = "token-2";
        HttpRequestDetails refreshed = authenticator.refreshRequestDetails(TARGET);

        assertEquals(2, server.tokenRequests.get());
        assertEquals("Bearer token-2", server.sessionAuthHeaders.get(1));
        assertEquals(StubAuthServer.SESSION_ID, refreshed.getClientCookies().get("JSESSIONID"));
    }

    @Test
    public void firstLoginUsesCachedTokenWithoutInvalidating() {
        server.acceptedBearer = "token-1";
        authenticator.refreshRequestDetails(TARGET);
        assertEquals(1, server.tokenRequests.get());
    }

    @Test
    public void openMrsRejectingTheTokenFails() {
        server.acceptedBearer = "some-other-token";
        try {
            authenticator.getRequestDetails(TARGET);
            fail("expected WebClientsException");
        } catch (WebClientsException expected) {
        }
    }

    @Test
    public void keycloakRejectingTheClientFailsWithoutCallingOpenMrs() {
        server.tokenStatus = 401;
        try {
            authenticator.getRequestDetails(TARGET);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("401"));
        }
        assertEquals(0, server.sessionRequests.get());
    }
}
