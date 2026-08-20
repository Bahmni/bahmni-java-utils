package org.bahmni.search.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.ALWAYS)
@AllArgsConstructor
public class PaginationResponse {

    @JsonProperty("next_cursor")
    private final String nextCursor;

    @JsonProperty("prev_cursor")
    private final String prevCursor;

}
