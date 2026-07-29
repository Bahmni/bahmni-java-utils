/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.model;

import lombok.Getter;

import java.util.Date;

@Getter
public class SearchResponseMeta {

    private final Date timestamp;

    public SearchResponseMeta() {
        this.timestamp = new Date();
    }
}
