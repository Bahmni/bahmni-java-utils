/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

public interface ContextSearchResponse {

    String getContext();

    SearchResponseMeta getMetaData();

    List<Map<String, Object>> getResults();

    List<Map<String, String>> getLinks();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    default SearchError getError() {
        return null;
    }

    default boolean isSuccess() {
        return getError() == null;
    }
}
