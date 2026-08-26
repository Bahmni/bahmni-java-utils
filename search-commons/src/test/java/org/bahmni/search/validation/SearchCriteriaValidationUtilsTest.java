/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.bahmni.search.validation;

import org.bahmni.search.cursor.CursorCodec;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.model.SearchRequestMeta;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SearchCriteriaValidationUtilsTest {

    private static final String ENTITY = "appointment";

    @Test
    public void shouldPassValidateEntityWhenEntityMatchesIgnoringCase() {
        SearchCriteriaValidationUtils.validateEntity("Appointment", ENTITY);
    }

    @Test
    public void shouldThrowWhenEntityIsNullOrEmpty() {
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateEntity(null, ENTITY));
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateEntity("", ENTITY));
    }

    @Test
    public void shouldThrowWhenEntityDoesNotMatchSupportedEntity() {
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateEntity("patient", ENTITY));
    }

    @Test
    public void shouldThrowWhenCriteriaIsNull() {
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateCriteria(null));
    }

    @Test
    public void shouldPassForValidLeafCondition() {
        SearchCriteriaValidationUtils.validateCriteria(leaf("status", "eq", "Scheduled"));
    }

    @Test
    public void shouldThrowWhenLeafConditionMissingComparator() {
        SearchCondition leaf = new SearchCondition();
        leaf.setField("status");
        leaf.setValue("Scheduled");

        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateCriteria(leaf));
    }

    @Test
    public void shouldThrowWhenLeafConditionMissingValue() {
        SearchCondition leaf = new SearchCondition();
        leaf.setField("status");
        leaf.setComparator("eq");

        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateCriteria(leaf));
    }

    @Test
    public void shouldPassForValidGroupCondition() {
        SearchCondition group = group("AND", leaf("status", "eq", "Scheduled"),
                leaf("patientId", "eq", "1"));

        SearchCriteriaValidationUtils.validateCriteria(group);
    }

    @Test
    public void shouldThrowWhenGroupConditionHasNoConditions() {
        SearchCondition group = new SearchCondition();
        group.setOperator("AND");
        group.setConditions(new ArrayList<>());

        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateCriteria(group));
    }

    @Test
    public void shouldThrowWhenGroupConditionMissingOperator() {
        SearchCondition group = new SearchCondition();
        group.setConditions(Collections.singletonList(leaf("status", "eq", "Scheduled")));

        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateCriteria(group));
    }

    @Test
    public void shouldPropagateErrorsFromNestedGroupConditions() {
        SearchCondition invalidChild = new SearchCondition();
        invalidChild.setField("status");

        SearchCondition group = group("AND", invalidChild);

        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateCriteria(group));
    }

    @Test
    public void shouldNotThrowWhenMetaOrPaginationIsNull() {
        SearchCriteriaValidationUtils.validateMeta(ENTITY, null);
        SearchCriteriaValidationUtils.validateMeta(ENTITY, new SearchRequestMeta());
    }

    @Test
    public void shouldPassForValidPaginationIncludingCaseInsensitiveValues() {
        SearchRequestMeta meta = metaWithPagination(10, "ASC", null, "NEXT");

        SearchCriteriaValidationUtils.validateMeta(ENTITY, meta);
    }

    @Test
    public void shouldThrowWhenLimitIsNonPositive() {
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateMeta(
                ENTITY, metaWithPagination(0, null, null, null)));
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateMeta(
                ENTITY, metaWithPagination(-5, null, null, null)));
    }

    @Test
    public void shouldThrowForUnsupportedSortOrder() {
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateMeta(
                ENTITY, metaWithPagination(null, "newest", null, null)));
    }

    @Test
    public void shouldThrowForUnsupportedDirection() {
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateMeta(
                ENTITY, metaWithPagination(null, null, null, "backwards")));
    }

    @Test
    public void shouldPassWhenCursorProvidedWithNextOrPrevDirection() {
        String cursor = CursorCodec.encode(ENTITY, 5L);

        SearchCriteriaValidationUtils.validateMeta(ENTITY, metaWithPagination(null, null, cursor, "next"));
        SearchCriteriaValidationUtils.validateMeta(ENTITY, metaWithPagination(null, null, cursor, "prev"));
    }

    @Test
    public void shouldThrowWhenCursorProvidedWithoutDirection() {
        String cursor = CursorCodec.encode(ENTITY, 5L);

        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateMeta(
                ENTITY, metaWithPagination(null, null, cursor, null)));
    }

    @Test
    public void shouldThrowWhenCursorBelongsToDifferentEntity() {
        String cursor = CursorCodec.encode("patient", 5L);

        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateMeta(
                ENTITY, metaWithPagination(null, null, cursor, "next")));
    }

    @Test
    public void shouldThrowWhenDirectionIsPrevWithoutCursor() {
        assertThrowsBadRequest(() -> SearchCriteriaValidationUtils.validateMeta(
                ENTITY, metaWithPagination(null, null, null, "prev")));
    }

    @Test
    public void shouldPassWhenDirectionIsNextOrAbsentWithoutCursor() {
        SearchCriteriaValidationUtils.validateMeta(ENTITY, metaWithPagination(null, null, null, "next"));
        SearchCriteriaValidationUtils.validateMeta(ENTITY, metaWithPagination(null, null, null, null));
    }

    private static SearchCondition leaf(String field, String comparator, String value) {
        SearchCondition leaf = new SearchCondition();
        leaf.setField(field);
        leaf.setComparator(comparator);
        leaf.setValue(value);
        return leaf;
    }

    private static SearchCondition group(String operator, SearchCondition... children) {
        SearchCondition group = new SearchCondition();
        group.setOperator(operator);
        List<SearchCondition> conditions = new ArrayList<>();
        Collections.addAll(conditions, children);
        group.setConditions(conditions);
        return group;
    }

    private static SearchRequestMeta metaWithPagination(Integer limit, String sortOrder,
                                                          String cursor, String direction) {
        PaginationRequest pagination = new PaginationRequest();
        pagination.setLimit(limit);
        pagination.setSortOrder(sortOrder);
        pagination.setCursor(cursor);
        pagination.setDirection(direction);

        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setPagination(pagination);
        return meta;
    }

    private static void assertThrowsBadRequest(Runnable runnable) {
        try {
            runnable.run();
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertTrue(e.getStatus().getCode() == 400);
        }
    }
}
