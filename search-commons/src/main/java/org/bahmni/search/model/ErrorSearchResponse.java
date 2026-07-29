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

public class ErrorSearchResponse implements ContextSearchResponse {

    private final String context;
    private final SearchError error;
    private final SearchResponseMeta meta;

    public ErrorSearchResponse(String context, int status, List<String> messages) {
        this.context = context;
        this.error = new SearchError(status, messages);
        this.meta = new SearchResponseMeta();
    }

    public ErrorSearchResponse(String context, int status, String message) {
        this(context, status, Collections.singletonList(message));
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
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, String>> getLinks() {
        return Collections.emptyList();
    }

    @Override
    public SearchError getError() {
        return error;
    }
}
