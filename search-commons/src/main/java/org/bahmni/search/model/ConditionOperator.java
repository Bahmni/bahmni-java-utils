/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.model;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;

public enum ConditionOperator {
    AND, OR;

    public static ConditionOperator resolve(String value) {
        if (value == null) return null;
        try {
            return ConditionOperator.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidSearchCriteriaException(
                    "Unknown operator: '" + value + "'. Supported: AND, OR",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
