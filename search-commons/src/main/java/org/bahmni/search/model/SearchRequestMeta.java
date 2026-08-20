package org.bahmni.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRequestMeta {

    private Boolean includeTotalCount;
    private PaginationRequest pagination;

}
