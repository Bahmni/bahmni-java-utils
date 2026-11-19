/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.webclients.WebClientsException;

import java.io.IOException;

public final class OAuthTokenResponseParser {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OAuthTokenResponseParser() {
    }

    public static String parseToken(String body, String tokenUrl) throws IOException {
        JsonNode json = OBJECT_MAPPER.readTree(body);
        JsonNode token = json == null ? null : json.get("access_token");
        if (token == null || token.asText().isEmpty()) {
            throw new WebClientsException(String.format("Token response from %s did not contain an access_token", tokenUrl));
        }
        return token.asText();
    }

    public static String describeError(String body) {
        try {
            JsonNode json = OBJECT_MAPPER.readTree(body);
            if (json != null && json.has("error")) {
                String description = json.has("error_description") ? ": " + json.get("error_description").asText() : "";
                return " (" + json.get("error").asText() + description + ")";
            }
        } catch (IOException ignored) {
            // body is not JSON; report the status only
        }
        return "";
    }
}
