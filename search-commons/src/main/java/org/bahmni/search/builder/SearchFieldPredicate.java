/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.builder;

import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

@FunctionalInterface
public interface SearchFieldPredicate {

    Predicate build(CriteriaBuilder criteriaBuilder, From<?, ?> root,
                    String fieldName, FieldComparator comparator,
                    String value, ConditionOperator operator);
}
