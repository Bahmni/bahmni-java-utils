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
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.PaginationResponse;
import org.bahmni.search.model.SearchRequestMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class PaginationHelper {

    private static final Logger log = LoggerFactory.getLogger(PaginationHelper.class);


    public static final String SORT_ORDER_ASC = "asc";
    public static final String SORT_ORDER_DESC = "desc";
    public static final String DIRECTION_PREV = "prev";
    public static final String HINT_PASS_DISTINCT_THROUGH = "hibernate.query.passDistinctThrough";

    private static final String DEFAULT_SORT_ORDER = SORT_ORDER_DESC;

    private PaginationHelper() {
    }

    public static PaginationRequest extractPaginationRequest(SearchRequestMeta meta) {
        if (meta != null && meta.getPagination() != null) {
            return meta.getPagination();
        }
        return new PaginationRequest();
    }

    public static int resolveEffectiveLimit(Integer requestedLimit, int defaultLimit, int maxLimit) {
        int limit = requestedLimit == null ? defaultLimit : requestedLimit;
        return Math.min(limit, maxLimit);
    }

    public static String resolveSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.isEmpty()) {
            return DEFAULT_SORT_ORDER;
        }
        return sortOrder.toLowerCase();
    }

    public static boolean isDescending(String sortOrder) {
        return !SORT_ORDER_ASC.equalsIgnoreCase(sortOrder);
    }

    public static Long decodeCursor(String entity, String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        return CursorCodec.decode(entity, cursor);
    }

    public static boolean isPrevDirection(String direction) {
        return DIRECTION_PREV.equalsIgnoreCase(direction);
    }

    public static boolean shouldSortQueryDescending(String sortOrder, String direction) {
        boolean isDesc = isDescending(sortOrder);
        boolean isPrev = isPrevDirection(direction);
        return isPrev != isDesc;
    }

    public static <T> List<T> trimToPageAndReorder(List<T> items, int limit, boolean isPrev) {
        boolean hasMore = items.size() > limit;
        List<T> result = hasMore
                ? new ArrayList<T>(items.subList(0, limit))
                : new ArrayList<T>(items);
        if (isPrev) {
            Collections.reverse(result);
        }
        return result;
    }

    public static boolean hasMore(int fetchedSize, int limit) {
        return fetchedSize > limit;
    }

    public static PaginationResponse buildPaginationResponse(
            String entity, long firstId, long lastId, boolean hasMore, Long requestCursorId, boolean isPrev) {
        String nextCursor;
        String prevCursor;

        if (isPrev) {
            nextCursor = CursorCodec.encode(entity, lastId);
            prevCursor = hasMore ? CursorCodec.encode(entity, firstId) : null;
        } else {
            nextCursor = hasMore ? CursorCodec.encode(entity, lastId) : null;
            prevCursor = requestCursorId != null ? CursorCodec.encode(entity, firstId) : null;
        }

        return new PaginationResponse(nextCursor, prevCursor);
    }

    public static PaginationResponse emptyPaginationResponse() {
        return new PaginationResponse(null, null);
    }

    public static Long resolveTotalCount(SearchRequestMeta meta, TotalCountSupplier countSupplier) {
        if (meta != null && Boolean.TRUE.equals(meta.getIncludeTotalCount())) {
            return countSupplier.count();
        }
        return null;
    }

    public static int resolveGlobalProperty(String rawValue, int fallback, String propertyName) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return fallback;
        }
        int value;
        try {
            value = Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid value '{}' for global property '{}'. Using fallback {}.", rawValue, propertyName, fallback);
            return fallback;
        }
        if (value <= 0) {
            log.warn("Global property '{}' must be a positive number, but was '{}'. Using fallback {}.",
                    propertyName, rawValue, fallback);
            return fallback;
        }
        return value;
    }

    public static ResolvedPagination resolvePaginationContext(SearchRequestMeta meta, String entity, int defaultLimit, int maxLimit) {
        PaginationRequest pagination = extractPaginationRequest(meta);
        int effectiveLimit = resolveEffectiveLimit(pagination.getLimit(), defaultLimit, maxLimit);
        String sortOrder = resolveSortOrder(pagination.getSortOrder());
        String direction = pagination.getDirection();
        Long cursorId = decodeCursor(entity, pagination.getCursor());
        boolean isPrev = isPrevDirection(direction);
        return new ResolvedPagination(cursorId, sortOrder, direction, isPrev, effectiveLimit);
    }

    public static <T> PageResult<T> paginate(String entity, List<T> rawResults,
            IdExtractor<T> idExtractor, ResolvedPagination resolved, boolean hasMore) {
        List<T> items = trimToPageAndReorder(rawResults, resolved.getEffectiveLimit(), resolved.isPrev());

        PaginationResponse paginationResponse = items.isEmpty()
                ? emptyPaginationResponse()
                : buildPaginationResponse(
                        entity,
                        idExtractor.extractId(items.get(0)),
                        idExtractor.extractId(items.get(items.size() - 1)),
                        hasMore, resolved.getCursorId(), resolved.isPrev());

        return new PageResult<>(items, paginationResponse);
    }

    public static <T, K> List<T> reorderByIds(List<T> items, List<K> orderedIds, Function<T, K> keyExtractor) {
        Map<K, Integer> positionByKey = new HashMap<>(orderedIds.size());
        for (int i = 0; i < orderedIds.size(); i++) {
            positionByKey.put(orderedIds.get(i), i);
        }

        List<T> result = new ArrayList<>(items);
        result.sort((first, second) -> {
            Integer firstPosition = positionOf(positionByKey, keyExtractor.apply(first));
            Integer secondPosition = positionOf(positionByKey, keyExtractor.apply(second));
            return Integer.compare(firstPosition, secondPosition);
        });
        return result;
    }

    private static <K> Integer positionOf(Map<K, Integer> positionByKey, K key) {
        Integer position = positionByKey.get(key);
        if (position == null) {
            throw new IllegalStateException("Item with id " + key + " was not found in the ordered id list");
        }
        return position;
    }


    public interface TotalCountSupplier {
        long count();
    }

    public interface IdExtractor<T> {
        long extractId(T item);
    }
}
