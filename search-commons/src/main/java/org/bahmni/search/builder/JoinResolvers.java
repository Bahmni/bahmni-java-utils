/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.builder;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;

/**
 * Common helpers used by module-specific {@code JoinResolver}s.
 */
public final class JoinResolvers {

    @SuppressWarnings("unchecked")
    public static From<?, ?> findExistingFetchOrJoin(From<?, ?> parent, String attributeName, JoinType joinType) {
        for (Fetch<?, ?> fetch : parent.getFetches()) {
            if (attributeName.equals(fetch.getAttribute().getName())) {
                return (From<?, ?>) fetch;
            }
        }
        return parent.join(attributeName, joinType);
    }
}
