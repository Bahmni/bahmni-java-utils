/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.pagination;

import org.bahmni.search.cursor.CursorCodec;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.PaginationResponse;
import org.bahmni.search.model.SearchRequestMeta;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaginationHelperTest {

    private static final String ENTITY = "appointment";

    @Test
    public void shouldExtractPaginationRequestFromMeta() {
        assertNull(PaginationHelper.extractPaginationRequest(null).getLimit());
        assertNull(PaginationHelper.extractPaginationRequest(new SearchRequestMeta()).getLimit());

        PaginationRequest pagination = new PaginationRequest();
        pagination.setLimit(25);
        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setPagination(pagination);
        assertEquals(Integer.valueOf(25), PaginationHelper.extractPaginationRequest(meta).getLimit());
    }

    @Test
    public void shouldResolveEffectiveLimitUsingDefaultsAndMax() {
        assertEquals(20, PaginationHelper.resolveEffectiveLimit(null, 20, 100));
        assertEquals(50, PaginationHelper.resolveEffectiveLimit(50, 20, 100));
        assertEquals(100, PaginationHelper.resolveEffectiveLimit(500, 20, 100));
        assertEquals(100, PaginationHelper.resolveEffectiveLimit(null, 500, 100));
    }

    @Test
    public void shouldResolveSortOrderWithDefaultAndLowercasing() {
        assertEquals("desc", PaginationHelper.resolveSortOrder(null));
        assertEquals("desc", PaginationHelper.resolveSortOrder(""));
        assertEquals("asc", PaginationHelper.resolveSortOrder("ASC"));
    }

    @Test
    public void shouldDetermineIsDescendingCaseInsensitively() {
        assertFalse(PaginationHelper.isDescending("asc"));
        assertFalse(PaginationHelper.isDescending("ASC"));
        assertTrue(PaginationHelper.isDescending("desc"));
        assertTrue(PaginationHelper.isDescending(null));
    }

    @Test
    public void shouldDecodeCursorOrReturnNullWhenAbsent() {
        assertNull(PaginationHelper.decodeCursor(ENTITY, null));
        assertNull(PaginationHelper.decodeCursor(ENTITY, ""));

        String cursor = CursorCodec.encode(ENTITY, 77L);
        assertEquals(Long.valueOf(77L), PaginationHelper.decodeCursor(ENTITY, cursor));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowWhenDecodingCursorForDifferentEntity() {
        String cursor = CursorCodec.encode("patient", 77L);

        PaginationHelper.decodeCursor(ENTITY, cursor);
    }

    @Test
    public void shouldDetermineIsPrevDirectionCaseInsensitively() {
        assertTrue(PaginationHelper.isPrevDirection("prev"));
        assertTrue(PaginationHelper.isPrevDirection("PREV"));
        assertFalse(PaginationHelper.isPrevDirection("next"));
        assertFalse(PaginationHelper.isPrevDirection(null));
    }

    @Test
    public void shouldSortAscendingQueryWhenSortOrderAscAndDirectionNext() {
        assertFalse(PaginationHelper.shouldSortQueryDescending("asc", "next"));
        assertFalse(PaginationHelper.shouldSortQueryDescending("asc", null));
    }

    @Test
    public void shouldSortDescendingQueryWhenSortOrderAscAndDirectionPrev() {
        assertTrue(PaginationHelper.shouldSortQueryDescending("asc", "prev"));
    }

    @Test
    public void shouldSortDescendingQueryWhenSortOrderDescAndDirectionNext() {
        assertTrue(PaginationHelper.shouldSortQueryDescending("desc", "next"));
        assertTrue(PaginationHelper.shouldSortQueryDescending("desc", null));
    }

    @Test
    public void shouldSortAscendingQueryWhenSortOrderDescAndDirectionPrev() {
        assertFalse(PaginationHelper.shouldSortQueryDescending("desc", "prev"));
    }

    @Test
    public void shouldTrimToLimitWhenResultsExceedLimit() {
        assertEquals(Arrays.asList(1, 2, 3),
                PaginationHelper.trimToPageAndReorder(Arrays.asList(1, 2, 3), 5, false));
        assertEquals(Arrays.asList(1, 2, 3),
                PaginationHelper.trimToPageAndReorder(Arrays.asList(1, 2, 3, 4), 3, false));
    }

    @Test
    public void shouldReverseAndTrimForPrevDirection() {
        assertEquals(Arrays.asList(3, 2, 1),
                PaginationHelper.trimToPageAndReorder(Arrays.asList(1, 2, 3), 5, true));
        assertEquals(Arrays.asList(3, 2, 1),
                PaginationHelper.trimToPageAndReorder(Arrays.asList(1, 2, 3, 4), 3, true));
    }

    @Test
    public void shouldNotMutateOriginalListWhenTrimming() {
        List<Integer> items = new ArrayList<>(Arrays.asList(1, 2, 3));

        PaginationHelper.trimToPageAndReorder(items, 5, true);

        assertEquals(Arrays.asList(1, 2, 3), items);
    }

    @Test
    public void shouldDetermineHasMoreBasedOnFetchedSizeVersusLimit() {
        assertTrue(PaginationHelper.hasMore(11, 10));
        assertFalse(PaginationHelper.hasMore(10, 10));
        assertFalse(PaginationHelper.hasMore(5, 10));
    }

    @Test
    public void shouldBuildForwardPaginationResponse() {
        PaginationResponse withMore = PaginationHelper.buildPaginationResponse(
                ENTITY, 1L, 10L, true, 99L, false);
        assertEquals(CursorCodec.encode(ENTITY, 10L), withMore.getNextCursor());
        assertEquals(CursorCodec.encode(ENTITY, 1L), withMore.getPrevCursor());

        PaginationResponse withoutMore = PaginationHelper.buildPaginationResponse(
                ENTITY, 1L, 10L, false, null, false);
        assertNull(withoutMore.getNextCursor());
        assertNull(withoutMore.getPrevCursor());
    }

    @Test
    public void shouldBuildPrevPaginationResponse() {
        PaginationResponse withMore = PaginationHelper.buildPaginationResponse(
                ENTITY, 1L, 10L, true, 50L, true);
        assertEquals(CursorCodec.encode(ENTITY, 10L), withMore.getNextCursor());
        assertEquals(CursorCodec.encode(ENTITY, 1L), withMore.getPrevCursor());

        PaginationResponse withoutMore = PaginationHelper.buildPaginationResponse(
                ENTITY, 1L, 10L, false, 50L, true);
        assertEquals(CursorCodec.encode(ENTITY, 10L), withoutMore.getNextCursor());
        assertNull(withoutMore.getPrevCursor());
    }

    @Test
    public void shouldBuildEmptyPaginationResponseWithNullCursors() {
        PaginationResponse response = PaginationHelper.emptyPaginationResponse();

        assertNull(response.getNextCursor());
        assertNull(response.getPrevCursor());
    }

    @Test
    public void shouldNotInvokeSupplierWhenTotalCountNotRequested() {
        PaginationHelper.TotalCountSupplier supplier = mock(PaginationHelper.TotalCountSupplier.class);

        assertNull(PaginationHelper.resolveTotalCount(null, supplier));

        SearchRequestMeta meta = new SearchRequestMeta();
        assertNull(PaginationHelper.resolveTotalCount(meta, supplier));

        meta.setIncludeTotalCount(false);
        assertNull(PaginationHelper.resolveTotalCount(meta, supplier));

        verify(supplier, never()).count();
    }

    @Test
    public void shouldInvokeSupplierWhenTotalCountRequested() {
        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setIncludeTotalCount(true);
        PaginationHelper.TotalCountSupplier supplier = mock(PaginationHelper.TotalCountSupplier.class);
        when(supplier.count()).thenReturn(42L);

        assertEquals(Long.valueOf(42L), PaginationHelper.resolveTotalCount(meta, supplier));
    }

    @Test
    public void shouldFallBackForBlankInvalidOrNonPositiveGlobalPropertyValues() {
        assertEquals(20, PaginationHelper.resolveGlobalProperty(null, 20, "p"));
        assertEquals(20, PaginationHelper.resolveGlobalProperty("  ", 20, "p"));
        assertEquals(20, PaginationHelper.resolveGlobalProperty("abc", 20, "p"));
        assertEquals(20, PaginationHelper.resolveGlobalProperty("0", 20, "p"));
        assertEquals(20, PaginationHelper.resolveGlobalProperty("-5", 20, "p"));
    }

    @Test
    public void shouldParseValidGlobalPropertyValue() {
        assertEquals(50, PaginationHelper.resolveGlobalProperty("50", 20, "p"));
        assertEquals(50, PaginationHelper.resolveGlobalProperty("  50  ", 20, "p"));
    }

    @Test
    public void shouldResolvePaginationContextWithDefaultsWhenMetaIsNull() {
        ResolvedPagination resolved = PaginationHelper.resolvePaginationContext(null, ENTITY, 20, 100);

        assertEquals(20, resolved.getEffectiveLimit());
        assertEquals("desc", resolved.getSortOrder());
        assertFalse(resolved.isPrev());
        assertNull(resolved.getCursorId());
        assertEquals(21, resolved.getFetchSize());
    }

    @Test
    public void shouldResolvePaginationContextFromProvidedMeta() {
        String cursor = CursorCodec.encode(ENTITY, 123L);
        PaginationRequest pagination = new PaginationRequest();
        pagination.setLimit(30);
        pagination.setSortOrder("ASC");
        pagination.setCursor(cursor);
        pagination.setDirection("prev");
        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setPagination(pagination);

        ResolvedPagination resolved = PaginationHelper.resolvePaginationContext(meta, ENTITY, 20, 100);

        assertEquals(30, resolved.getEffectiveLimit());
        assertEquals("asc", resolved.getSortOrder());
        assertTrue(resolved.isPrev());
        assertEquals(Long.valueOf(123L), resolved.getCursorId());
        assertEquals(31, resolved.getFetchSize());
    }

    @Test
    public void shouldClampEffectiveLimitWithinResolvePaginationContext() {
        PaginationRequest pagination = new PaginationRequest();
        pagination.setLimit(1000);
        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setPagination(pagination);

        assertEquals(100, PaginationHelper.resolvePaginationContext(meta, ENTITY, 20, 100).getEffectiveLimit());
    }

    @Test
    public void shouldPaginateAndBuildResponseWhenMoreExists() {
        List<Long> rawResults = Arrays.asList(10L, 9L, 8L, 7L);
        ResolvedPagination resolved = new ResolvedPagination(null, "desc", "next", false, 3);

        PageResult<Long> result = PaginationHelper.paginate(ENTITY, rawResults, id -> id, resolved, true);

        assertEquals(Arrays.asList(10L, 9L, 8L), result.getItems());
        assertEquals(CursorCodec.encode(ENTITY, 8L), result.getPaginationResponse().getNextCursor());
        assertNull(result.getPaginationResponse().getPrevCursor());
    }

    @Test
    public void shouldPaginateAndReturnEmptyResponseWhenNoItems() {
        ResolvedPagination resolved = new ResolvedPagination(null, "desc", "next", false, 3);

        PageResult<Long> result = PaginationHelper.paginate(ENTITY, new ArrayList<>(), id -> id, resolved, false);

        assertTrue(result.getItems().isEmpty());
        assertNull(result.getPaginationResponse().getNextCursor());
        assertNull(result.getPaginationResponse().getPrevCursor());
    }

    @Test
    public void shouldPaginateAndReverseForPrevDirection() {
        List<Long> rawResults = Arrays.asList(5L, 6L, 7L);
        ResolvedPagination resolved = new ResolvedPagination(20L, "asc", "prev", true, 3);

        PageResult<Long> result = PaginationHelper.paginate(ENTITY, rawResults, id -> id, resolved, false);

        assertEquals(Arrays.asList(7L, 6L, 5L), result.getItems());
        assertEquals(CursorCodec.encode(ENTITY, 5L), result.getPaginationResponse().getNextCursor());
        assertNull(result.getPaginationResponse().getPrevCursor());
    }

    @Test
    public void shouldReorderItemsAccordingToIdOrderWithoutMutatingOriginal() {
        List<String> items = new ArrayList<>(Arrays.asList("c", "a", "b"));
        List<Integer> orderedIds = Arrays.asList(1, 2, 3);

        List<String> result = PaginationHelper.reorderByIds(items, orderedIds,
                item -> {
                    switch (item) {
                        case "a": return 1;
                        case "b": return 2;
                        case "c": return 3;
                        default: throw new IllegalStateException();
                    }
                });

        assertEquals(Arrays.asList("a", "b", "c"), result);
        assertEquals(Arrays.asList("c", "a", "b"), items);
    }

    @Test
    public void shouldThrowWhenItemKeyNotFoundInOrderedIds() {
        List<String> items = Arrays.asList("a", "unknown");
        List<Integer> orderedIds = Arrays.asList(1, 2);

        try {
            PaginationHelper.reorderByIds(items, orderedIds, item -> "a".equals(item) ? 1 : 999);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("999"));
        }
    }
}
