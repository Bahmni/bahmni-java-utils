package org.bahmni.search.cursor;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CursorCodec {

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    private CursorCodec() {
    }

    /**
     * Encodes an entity id into an opaque cursor string.
     * Cursor content: base64 of {"id": <id>}
     */
    public static String encode(long id) {
        String json = "{\"id\":" + id + "}";
        return ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes an opaque cursor string back to the entity id.
     * Expects base64-encoded JSON: {"id": <id>}
     */
    public static long decode(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Cursor must not be null or empty",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        try {
            byte[] decoded = DECODER.decode(cursor);
            String json = new String(decoded, StandardCharsets.UTF_8);
            Matcher matcher = ID_PATTERN.matcher(json);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
            throw new InvalidSearchCriteriaException(
                    "Invalid cursor: missing 'id' field",
                    SearchResponseErrorStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            throw new InvalidSearchCriteriaException(
                    "Invalid cursor: not a valid encoded value",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
