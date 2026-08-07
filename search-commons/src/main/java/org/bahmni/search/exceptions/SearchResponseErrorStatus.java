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
public enum SearchResponseErrorStatus {

    BAD_REQUEST(400),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    SearchResponseErrorStatus(int code) {
        this.code = code;
    }
}
