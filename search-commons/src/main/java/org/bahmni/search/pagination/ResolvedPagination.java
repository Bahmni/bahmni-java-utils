package org.bahmni.search.pagination;

import lombok.Getter;

@Getter
public final class ResolvedPagination {

    private final Long cursorId;
    private final String sortOrder;
    private final String direction;
    private final boolean prev;
    private final int effectiveLimit;

    public ResolvedPagination(Long cursorId, String sortOrder, String direction, boolean prev, int effectiveLimit) {
        this.cursorId = cursorId;
        this.sortOrder = sortOrder;
        this.direction = direction;
        this.prev = prev;
        this.effectiveLimit = effectiveLimit;
    }

    public int getFetchSize() {
        return effectiveLimit + 1;
    }
}
