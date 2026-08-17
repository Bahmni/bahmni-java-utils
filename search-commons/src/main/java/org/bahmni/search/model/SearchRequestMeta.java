package org.bahmni.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRequestMeta {

    private Boolean includeTotalCount;
    private PaginationRequest pagination;

    public Boolean getIncludeTotalCount() {
        return includeTotalCount;
    }

    public void setIncludeTotalCount(Boolean includeTotalCount) {
        this.includeTotalCount = includeTotalCount;
    }

    public PaginationRequest getPagination() {
        return pagination;
    }

    public void setPagination(PaginationRequest pagination) {
        this.pagination = pagination;
    }
}
