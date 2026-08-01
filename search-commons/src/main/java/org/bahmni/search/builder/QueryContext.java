/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.builder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryContext<T> {

    public final CriteriaBuilder criteriaBuilder;
    public final Root<T> root;
    public final List<Predicate> predicates;
    public final Map<String, From<?, ?>> joinCache = new HashMap<>();

    public QueryContext(CriteriaBuilder criteriaBuilder, Root<T> root, List<Predicate> predicates) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
        this.predicates = predicates;
    }
}
