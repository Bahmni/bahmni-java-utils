package org.bahmni.search.pagination;

import org.bahmni.search.cursor.CursorCodec;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.PaginationResponse;
import org.bahmni.search.model.SearchRequestMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PaginationHelper {

    public static final String SORT_ORDER_ASC = "asc";
    public static final String SORT_ORDER_DESC = "desc";
    public static final String DIRECTION_PREV = "prev";
    public static final String DIRECTION_NEXT = "next";
    public static final String HINT_PASS_DISTINCT_THROUGH = "hibernate.query.passDistinctThrough";

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;
    private static final String DEFAULT_SORT_ORDER = SORT_ORDER_DESC;

    private PaginationHelper() {
    }

    public static PaginationRequest resolvePagination(SearchRequestMeta meta) {
        if (meta != null && meta.getPagination() != null) {
            return meta.getPagination();
        }
        return new PaginationRequest();
    }

    public static int resolveEffectiveLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.min(requestedLimit, MAX_LIMIT);
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

    public static Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        return CursorCodec.decode(cursor);
    }

    public static boolean isPrevDirection(String direction) {
        return DIRECTION_PREV.equalsIgnoreCase(direction);
    }

    public static boolean resolveQueryDescending(String sortOrder, String direction) {
        boolean isDesc = isDescending(sortOrder);
        boolean isPrev = isPrevDirection(direction);
        return isPrev != isDesc;
    }

    public static <T> List<T> trimAndOrient(List<T> items, int limit, boolean isPrev) {
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
            long firstId, long lastId, boolean hasMore, Long cursorId, boolean isPrev) {
        String nextCursor;
        String prevCursor;

        if (isPrev) {
            nextCursor = CursorCodec.encode(lastId);
            prevCursor = hasMore ? CursorCodec.encode(firstId) : null;
        } else {
            nextCursor = hasMore ? CursorCodec.encode(lastId) : null;
            prevCursor = cursorId != null ? CursorCodec.encode(firstId) : null;
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

    public interface TotalCountSupplier {
        long count();
    }
}
