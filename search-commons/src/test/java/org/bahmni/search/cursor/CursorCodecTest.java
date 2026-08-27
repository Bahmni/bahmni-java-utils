/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.cursor;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CursorCodecTest {

    private static final String ENTITY = "appointment";

    @Test
    public void shouldRoundTripEncodeAndDecode() {
        String cursor = CursorCodec.encode(ENTITY, 42L);

        assertEquals(42L, CursorCodec.decode(ENTITY, cursor));
        assertEquals(42L, CursorCodec.decode("APPOINTMENT", cursor));
    }

    @Test
    public void shouldThrowWhenEntityIsNullOrEmpty() {
        assertThrowsIllegalArgument(() -> CursorCodec.encode(null, 1L));
        assertThrowsIllegalArgument(() -> CursorCodec.encode("", 1L));
        assertThrowsIllegalArgument(() -> CursorCodec.decode(null, "any"));
    }

    @Test
    public void shouldThrowWhenCursorIsNullOrEmpty() {
        assertThrowsInvalidCursor(() -> CursorCodec.decode(ENTITY, null));
        assertThrowsInvalidCursor(() -> CursorCodec.decode(ENTITY, ""));
    }

    @Test
    public void shouldThrowWhenCursorIsNotValidBase64() {
        assertThrowsInvalidCursor(() -> CursorCodec.decode(ENTITY, "not-valid-base64-!!!"));
    }

    @Test
    public void shouldThrowWhenDecodedCursorHasNoSeparator() {
        String malformed = encodeRaw("noSeparatorHere");

        assertThrowsInvalidCursor(() -> CursorCodec.decode(ENTITY, malformed));
    }

    @Test
    public void shouldThrowWhenCursorBelongsToDifferentEntity() {
        String cursor = CursorCodec.encode("patient", 7L);

        try {
            CursorCodec.decode("appointment", cursor);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertTrue(e.getMessage().contains("does not belong to entity"));
        }
    }

    @Test
    public void shouldThrowWhenIdPortionIsNotNumeric() {
        String malformed = encodeRaw(ENTITY + ":not-a-number");

        assertThrowsInvalidCursor(() -> CursorCodec.decode(ENTITY, malformed));
    }

    private static String encodeRaw(String raw) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static void assertThrowsIllegalArgument(Runnable runnable) {
        try {
            runnable.run();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("entity"));
        }
    }

    private static void assertThrowsInvalidCursor(Runnable runnable) {
        try {
            runnable.run();
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertEquals(400, e.getStatus().getCode());
        }
    }
}
