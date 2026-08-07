/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.model;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum FieldType {

    STRING(EnumSet.of(FieldComparator.EQ)),
    DATE(EnumSet.of(FieldComparator.GT, FieldComparator.LT, FieldComparator.GE, FieldComparator.LE));

    private final Set<FieldComparator> supportedComparators;

    FieldType(Set<FieldComparator> supportedComparators) {
        this.supportedComparators = supportedComparators;
    }

    public boolean supports(FieldComparator comparator) {
        return supportedComparators.contains(comparator);
    }
}
