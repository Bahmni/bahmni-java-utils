package org.bahmni.search.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponseMeta {

    private final long timestamp;
    private final Long totalCount;
    private final PaginationResponse pagination;

    public SearchResponseMeta() {
        this.timestamp = System.currentTimeMillis();
        this.totalCount = null;
        this.pagination = new PaginationResponse(null, null);
    }

    public SearchResponseMeta(PaginationResponse pagination, Long totalCount) {
        this.timestamp = System.currentTimeMillis();
        this.pagination = pagination;
        this.totalCount = totalCount;
    }

}
