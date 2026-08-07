
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 * <p>
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class SearchCondition {

    @Setter
    private String field;

    private FieldComparator comparator;
    
    @Setter
    private String value;

    private ConditionOperator operator;

    @Setter
    private List<SearchCondition> conditions;

    public void setComparator(String value) {
        this.comparator = FieldComparator.resolve(value);
    }

    public void setOperator(String value) {
        this.operator = ConditionOperator.resolve(value);
    }

    public boolean isLeaf() {
        return field != null;
    }

    public boolean isGroup() {
        return conditions != null && !conditions.isEmpty() && field == null;
    }
}
