package org.bahmni.search.pagination;

import lombok.Getter;
import org.bahmni.search.model.PaginationResponse;

import java.util.List;

@Getter
public final class PageResult<T> {

    private final List<T> items;
    private final PaginationResponse paginationResponse;

    public PageResult(List<T> items, PaginationResponse paginationResponse) {
        this.items = items;
        this.paginationResponse = paginationResponse;
    }

}
