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
import org.bahmni.webclients.HttpRequestDetails;
import org.bahmni.webclients.WebClientsException;
import org.bahmni.webclients.oauth.StubAuthServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OpenMRSAuthenticatorTest {
    private StubAuthServer server;
    private ConnectionDetails details;

    @Before
    public void setUp() throws Exception {
        server = new StubAuthServer();
        details = new ConnectionDetails(server.sessionUrl(), "admin", "Admin123", 2000, 5000);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    private Properties oauthProperties(String enabled) {
        Properties p = new Properties();
        if (enabled != null) p.setProperty("oauth2.enabled", enabled);
        p.setProperty("openmrs.oauth.token.url", server.tokenUrl());
        p.setProperty("openmrs.oauth.client.id", "odoo-connect-sa");
        p.setProperty("openmrs.oauth.client.secret", "secret");
        return p;
    }

    @Test
    public void flagOffYieldsPlainBasicAuthenticatorAndNeverTouchesTokenEndpoint() {
        String[] offValues = {null, "", "  ", "false", "FALSE", "no", "yes", "1", "on", "tru", "garbage"};
        for (String value : offValues) {
            Authenticator authenticator = OpenMRSAuthenticator.getAuthenticator(details, oauthProperties(value));
            assertSame("value: " + value, OpenMRSBasicLoginAuthenticator.class, authenticator.getClass());
        }
        assertEquals(0, server.tokenRequests.get());
    }

    @Test
    public void flagOffNeedsNoOAuthSettingsAtAll() {
        assertSame(OpenMRSBasicLoginAuthenticator.class, OpenMRSAuthenticator.getAuthenticator(details, new Properties()).getClass());
        assertSame(OpenMRSBasicLoginAuthenticator.class, OpenMRSAuthenticator.getAuthenticator(details, null).getClass());
    }

    @Test
    public void flagOffAuthenticatorStillLogsInWithBasicAndNeverCallsKeycloak() {
        Authenticator authenticator = OpenMRSAuthenticator.getAuthenticator(details, oauthProperties("false"));

        HttpRequestDetails request = authenticator.getRequestDetails(URI.create("http://openmrs.test/x"));

        assertTrue(server.sessionAuthHeaders.get(0).startsWith("Basic "));
        assertEquals(StubAuthServer.SESSION_ID, request.getClientCookies().get("JSESSIONID"));
        assertEquals(0, server.tokenRequests.get());
    }

    @Test
    public void flagTrueInAnyCaseSelectsOAuthAuthenticator() {
        for (String value : new String[]{"true", "TRUE", "True", " true "}) {
            Authenticator authenticator = OpenMRSAuthenticator.getAuthenticator(details, oauthProperties(value));
            assertSame("value: " + value, OpenMRSOAuthLoginAuthenticator.class, authenticator.getClass());
        }
    }

    @Test
    public void flagTrueEndToEndUsesBearer() {
        server.acceptedBearer = "token-1";
        Authenticator authenticator = OpenMRSAuthenticator.getAuthenticator(details, oauthProperties("true"));

        HttpRequestDetails request = authenticator.getRequestDetails(URI.create("http://openmrs.test/x"));

        assertEquals("Bearer token-1", server.sessionAuthHeaders.get(0));
        assertEquals(StubAuthServer.SESSION_ID, request.getClientCookies().get("JSESSIONID"));
    }

    @Test
    public void flagTrueWithMissingSettingsFailsNamingTheKeysButNotValues() {
        Properties p = new Properties();
        p.setProperty("oauth2.enabled", "true");
        p.setProperty("openmrs.oauth.client.secret", "topsecret");
        try {
            OpenMRSAuthenticator.getAuthenticator(details, p);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("openmrs.oauth.token.url"));
            assertTrue(e.getMessage(), e.getMessage().contains("openmrs.oauth.client.id"));
            assertFalse(e.getMessage().contains("openmrs.oauth.client.secret"));
            assertFalse(e.getMessage().contains("topsecret"));
        }
    }

    @Test
    public void flagTrueWithEmptyValuesCountsAsMissing() {
        Properties p = oauthProperties("true");
        p.setProperty("openmrs.oauth.client.secret", "");
        try {
            OpenMRSAuthenticator.getAuthenticator(details, p);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("openmrs.oauth.client.secret"));
        }
    }

    @Test
    public void invalidSkewIsReportedWhenEnabled() {
        Properties p = oauthProperties("true");
        p.setProperty("openmrs.oauth.expiry.skew.seconds", "soon");
        try {
            OpenMRSAuthenticator.getAuthenticator(details, p);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("expiry.skew"));
        }
    }
}
