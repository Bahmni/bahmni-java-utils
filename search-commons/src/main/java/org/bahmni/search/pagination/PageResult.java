/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.pagination;

import lombok.Getter;
import org.bahmni.search.model.PaginationResponse;

import java.util.List;

@Getter
public final class PageResult<T> {

    private final List<T> items;
    private final PaginationResponse paginationResponse;

    public PageResult(List<T> items, PaginationResponse paginationResponse) {
        this.items = items;
        this.paginationResponse = paginationResponse;
    }

}
