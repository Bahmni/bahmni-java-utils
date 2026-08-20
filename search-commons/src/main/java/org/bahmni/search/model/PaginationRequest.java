package org.bahmni.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationRequest {

    private Integer limit;
    private String sortOrder;
    private String cursor;
    private String direction;

}
