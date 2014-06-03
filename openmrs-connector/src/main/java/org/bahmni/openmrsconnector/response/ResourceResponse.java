package org.bahmni.openmrsconnector.response;

import java.util.List;

public class ResourceResponse {
    private List<Resource> results;

    public List<Resource> getResults() {
        return results;
    }

    public void setResults(List<Resource> results) {
        this.results = results;
    }
}