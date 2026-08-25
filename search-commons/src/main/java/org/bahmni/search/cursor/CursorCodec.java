/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.cursor;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encodes/decodes pagination cursors.
 * <p>
 * Cursors are namespaced by an {@code entity} identifier so that a cursor generated for one
 * entity (e.g. "patient") cannot be silently reused against a different entity's search
 * endpoint (e.g. "appointment"), even if the underlying numeric id happens to collide.
 * <p>
 * Encoded format (before base64url encoding): {@code <entity>:<id>}
 */
public final class CursorCodec {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String SEPARATOR = ":";

    private CursorCodec() {
    }

    public static String encode(String entity, long id) {
        validateEntity(entity);
        String raw = entity + SEPARATOR + id;
        return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static long decode(String entity, String cursor) {
        validateEntity(entity);
        if (cursor == null || cursor.isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Cursor must not be null or empty",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        String decodedRaw;
        try {
            byte[] decoded = DECODER.decode(cursor);
            decodedRaw = new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new InvalidSearchCriteriaException(
                    "Invalid cursor: not a valid encoded value",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        int separatorIndex = decodedRaw.lastIndexOf(SEPARATOR);
        if (separatorIndex < 0) {
            throw new InvalidSearchCriteriaException(
                    "Invalid cursor: not a valid encoded value",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        String decodedEntity = decodedRaw.substring(0, separatorIndex);
        String decodedId = decodedRaw.substring(separatorIndex + 1);

        if (!entity.equalsIgnoreCase(decodedEntity)) {
            throw new InvalidSearchCriteriaException(
                    "Invalid cursor: does not belong to entity '" + entity + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        try {
            return Long.parseLong(decodedId);
        } catch (NumberFormatException e) {
            throw new InvalidSearchCriteriaException(
                    "Invalid cursor: not a valid encoded value",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private static void validateEntity(String entity) {
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("entity must not be null or empty");
        }
    }
}
