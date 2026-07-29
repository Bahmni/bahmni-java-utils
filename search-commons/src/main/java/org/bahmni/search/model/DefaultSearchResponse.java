/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultSearchResponse implements ContextSearchResponse {

    private final String context;
    private final SearchResponseMeta meta;
    private final List<Map<String, Object>> results;
    private final List<Map<String, String>> links;

    public DefaultSearchResponse(String context, List<Map<String, Object>> results) {
        this.context = context;
        this.results = results;
        this.meta = new SearchResponseMeta();
        this.links = Collections.emptyList();
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public SearchResponseMeta getMetaData() {
        return meta;
    }

    @Override
    public List<Map<String, Object>> getResults() {
        return results;
    }

    @Override
    public List<Map<String, String>> getLinks() {
        return links;
    }
}
