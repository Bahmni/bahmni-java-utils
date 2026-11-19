/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.oauth;

import org.bahmni.webclients.OAuthToken;

import org.bahmni.webclients.OAuth2ConnectionDetails;

import org.bahmni.webclients.WebClientsException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpOAuthTokenClientTest {
    private StubAuthServer server;

    @Before
    public void setUp() throws Exception {
        server = new StubAuthServer();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    private OAuth2ConnectionDetails credentials(String scope) {
        return new OAuth2ConnectionDetails(null, server.tokenUrl(), "odoo-connect-sa", "s3cr3t/+=", scope, 2000, 5000, 30);
    }

    private OAuthToken fetch(String scope) {
        return HttpOAuthTokenClient.fetchToken(credentials(scope));
    }

    @Test
    public void fetchesTokenWithClientCredentialsGrantAndBasicClientAuth() {
        OAuthToken token = fetch(null);

        assertEquals("token-1", token.getAccessToken());
        assertEquals(300, token.getExpiresInSeconds());
        assertEquals("grant_type=client_credentials", server.tokenBodies.get(0));
        String expected = "Basic " + Base64.getEncoder().encodeToString("odoo-connect-sa:s3cr3t%2F%2B%3D".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, server.tokenAuthHeaders.get(0));
    }

    @Test
    public void sendsScopeWhenConfigured() {
        fetch("openid");
        assertTrue(server.tokenBodies.get(0).contains("scope=openid"));
    }

    @Test
    public void rejectedClientFailsWithStatusAndErrorButNeverTheSecret() {
        server.tokenStatus = 401;
        try {
            fetch(null);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("HTTP 401"));
            assertTrue(e.getMessage(), e.getMessage().contains("unauthorized_client"));
            assertFalse(e.getMessage().contains("s3cr3t"));
        }
    }

    @Test
    public void responseWithoutAccessTokenFails() {
        server.omitAccessToken = true;
        try {
            fetch(null);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("access_token"));
        }
    }

    @Test
    public void unreachableTokenEndpointFailsWithoutLeakingSecret() {
        server.stop();
        try {
            fetch(null);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertFalse(e.getMessage().contains("s3cr3t"));
        }
    }
}