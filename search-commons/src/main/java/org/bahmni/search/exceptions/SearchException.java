/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.exceptions;

import lombok.Getter;

@Getter
public class SearchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final SearchResponseErrorStatus status;

    public SearchException(String message, Throwable cause) {
        super(message, cause);
        this.status = SearchResponseErrorStatus.INTERNAL_SERVER_ERROR;
    }
}
