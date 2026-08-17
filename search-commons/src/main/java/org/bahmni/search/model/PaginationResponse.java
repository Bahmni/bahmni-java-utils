package org.bahmni.search.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class PaginationResponse {

    @JsonProperty("next_cursor")
    private final String nextCursor;

    @JsonProperty("prev_cursor")
    private final String prevCursor;

    public PaginationResponse(String nextCursor, String prevCursor) {
        this.nextCursor = nextCursor;
        this.prevCursor = prevCursor;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public String getPrevCursor() {
        return prevCursor;
    }
}
