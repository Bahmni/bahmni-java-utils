/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.oauth;

import org.bahmni.webclients.OAuthToken;

import org.bahmni.webclients.WebClientsException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OAuthTokenResponseParserTest {
    private static final String TOKEN_URL = "http://keycloak.test/token";

    @Test
    public void parsesAccessTokenAndLifetime() throws IOException {
        OAuthToken token = OAuthTokenResponseParser.parseToken("{\"access_token\":\"abc\",\"expires_in\":120}", TOKEN_URL);
        assertEquals("abc", token.getAccessToken());
        assertEquals(120, token.getExpiresInSeconds());
    }

    @Test
    public void missingExpiresInDefaultsToSixtySeconds() throws IOException {
        OAuthToken token = OAuthTokenResponseParser.parseToken("{\"access_token\":\"abc\"}", TOKEN_URL);
        assertEquals(OAuthTokenResponseParser.DEFAULT_LIFETIME_SECONDS, token.getExpiresInSeconds());
    }

    @Test
    public void emptyAccessTokenIsRejected() throws IOException {
        try {
            OAuthTokenResponseParser.parseToken("{\"access_token\":\"\"}", TOKEN_URL);
            fail("expected WebClientsException");
        } catch (WebClientsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(TOKEN_URL));
        }
    }

    @Test
    public void describeErrorIncludesCodeAndDescription() {
        assertEquals(" (invalid_client: bad secret)",
                OAuthTokenResponseParser.describeError("{\"error\":\"invalid_client\",\"error_description\":\"bad secret\"}"));
    }

    @Test
    public void describeErrorIgnoresNonJsonBody() {
        assertEquals("", OAuthTokenResponseParser.describeError("<html>gateway timeout</html>"));
    }
}
