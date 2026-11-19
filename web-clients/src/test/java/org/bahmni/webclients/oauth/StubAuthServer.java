/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.oauth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

/** Minimal in-JVM stand-in for Keycloak (/token) and OpenMRS (/session). */
public class StubAuthServer {
    public static final String SESSION_ID = "0123456789ABCDEF0123456789ABCDEF";

    private final HttpServer server;
    public final AtomicInteger tokenRequests = new AtomicInteger();
    public final AtomicInteger sessionRequests = new AtomicInteger();
    public final List<String> tokenAuthHeaders = new CopyOnWriteArrayList<String>();
    public final List<String> tokenBodies = new CopyOnWriteArrayList<String>();
    public final List<String> sessionAuthHeaders = new CopyOnWriteArrayList<String>();

    public volatile int tokenStatus = 200;
    public volatile String tokenErrorBody = "{\"error\":\"unauthorized_client\",\"error_description\":\"Invalid client or Invalid client credentials\"}";
    public volatile int expiresIn = 300;
    public volatile long tokenDelayMillis = 0;
    public volatile boolean omitAccessToken = false;
    /** Session endpoint accepts only "Bearer <this>" when set, or only Basic when null. */
    public volatile String acceptedBearer = null;

    public StubAuthServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/token", exchange -> {
            int n = tokenRequests.incrementAndGet();
            tokenAuthHeaders.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
            tokenBodies.add(read(exchange.getRequestBody()));
            sleep(tokenDelayMillis);
            if (tokenStatus != 200) {
                respond(exchange, tokenStatus, tokenErrorBody, null);
            } else if (omitAccessToken) {
                respond(exchange, 200, "{\"token_type\":\"Bearer\"}", null);
            } else {
                respond(exchange, 200, "{\"access_token\":\"token-" + n + "\",\"expires_in\":" + expiresIn + ",\"token_type\":\"Bearer\"}", null);
            }
        });
        server.createContext("/session", exchange -> {
            sessionRequests.incrementAndGet();
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            sessionAuthHeaders.add(String.valueOf(auth));
            boolean ok = acceptedBearer == null
                    ? auth != null && auth.startsWith("Basic ")
                    : auth != null && auth.equals("Bearer " + acceptedBearer);
            if (ok) {
                respond(exchange, 200, "{\"authenticated\":true}", "JSESSIONID=" + SESSION_ID + "; Path=/openmrs; HttpOnly");
            } else {
                respond(exchange, 401, "{\"authenticated\":false}", null);
            }
        });
        server.start();
    }

    public String tokenUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/token";
    }

    public String sessionUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/session";
    }

    public void stop() {
        server.stop(0);
    }

    private static void respond(HttpExchange exchange, int status, String body, String setCookie) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        if (setCookie != null) exchange.getResponseHeaders().add("Set-Cookie", setCookie);
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int r;
        while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void sleep(long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
