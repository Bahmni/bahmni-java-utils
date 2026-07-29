/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.service;

import org.bahmni.search.model.ContextSearchResponse;
import org.bahmni.search.model.SearchRequest;

public interface SearchService {

    String getEntity();

    ContextSearchResponse search(SearchRequest request);
}
