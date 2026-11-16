/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.oauth;

import org.bahmni.webclients.WebClientsException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientCredentialsTokenProviderTest {
    private static final int SKEW_SECONDS = 30;
    private static final long START_MILLIS = 1_000_000L;

    private StubAuthServer server;
    private Clock clock;
    private long nowMillis;

    @Before
    public void setUp() throws Exception {
        server = new StubAuthServer();
        nowMillis = START_MILLIS;
        clock = mock(Clock.class);
        when(clock.millis()).thenAnswer(invocation -> nowMillis);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    private ClientCredentialsTokenProvider provider() {
        return new ClientCredentialsTokenProvider(
                new OAuthClientCredentials(server.tokenUrl(), "odoo-connect-sa", "secret", null, 2000, 5000, SKEW_SECONDS),
                clock);
    }

    private void advanceSeconds(long seconds) {
        nowMillis += seconds * 1000;
    }

    @Test
    public void cachesTokenUntilExpiryMinusSkew() {
        ClientCredentialsTokenProvider provider = provider();
        assertEquals("token-1", provider.getAccessToken());

        advanceSeconds(269);
        assertEquals("token-1", provider.getAccessToken());
        assertEquals(1, server.tokenRequests.get());

        advanceSeconds(1);
        assertEquals("token-2", provider.getAccessToken());
        assertEquals(2, server.tokenRequests.get());
    }

    @Test
    public void invalidateForcesNewToken() {
        ClientCredentialsTokenProvider provider = provider();
        provider.getAccessToken();
        provider.invalidate();
        assertEquals("token-2", provider.getAccessToken());
    }

    @Test
    public void shortLivedTokenIsStillCachedForHalfItsLifetime() {
        server.expiresIn = 20;
        ClientCredentialsTokenProvider provider = provider();
        provider.getAccessToken();
        advanceSeconds(9);
        provider.getAccessToken();
        assertEquals(1, server.tokenRequests.get());
        advanceSeconds(1);
        provider.getAccessToken();
        assertEquals(2, server.tokenRequests.get());
    }

    @Test
    public void concurrentCallersTriggerSingleTokenRequest() throws Exception {
        server.tokenDelayMillis = 200;
        ClientCredentialsTokenProvider provider = provider();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<String>> results = new ArrayList<Future<String>>();
            for (int i = 0; i < 8; i++) {
                results.add(pool.submit(() -> provider.getAccessToken()));
            }
            for (Future<String> result : results) assertEquals("token-1", result.get());
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, server.tokenRequests.get());
    }

    @Test
    public void failedFetchIsNotCachedSoNextCallRetries() {
        server.tokenStatus = 500;
        ClientCredentialsTokenProvider provider = provider();
        try {
            provider.getAccessToken();
        } catch (WebClientsException expected) {
            // the next call must go back to the token endpoint
        }
        server.tokenStatus = 200;
        assertEquals("token-2", provider.getAccessToken());
    }
}
