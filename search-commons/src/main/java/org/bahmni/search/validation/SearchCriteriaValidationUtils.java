package org.bahmni.search.validation;

import org.bahmni.search.cursor.CursorCodec;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.model.SearchRequestMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SearchCriteriaValidationUtils {

    private static final Set<FieldComparator> SUPPORTED_COMPARATORS =
            EnumSet.of(FieldComparator.EQ, FieldComparator.GT, FieldComparator.LT,
                    FieldComparator.GE, FieldComparator.LE);

    private static final Set<ConditionOperator> SUPPORTED_OPERATORS =
            EnumSet.of(ConditionOperator.AND, ConditionOperator.OR);

    private static final Set<String> SUPPORTED_SORT_ORDERS =
            new HashSet<>(Arrays.asList("asc", "desc"));

    private static final Set<String> SUPPORTED_DIRECTIONS =
            new HashSet<>(Arrays.asList("next", "prev"));

    private SearchCriteriaValidationUtils() {
    }

    public static void validateEntity(String entity, String supportedEntity) {
        if (entity == null || entity.isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Request must include 'entity'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        if (!supportedEntity.equalsIgnoreCase(entity)) {
            throw new InvalidSearchCriteriaException(
                    "Entity '" + entity + "' is not supported. Supported entities: ["
                            + supportedEntity + "]",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    public static void validateCriteria(SearchCondition criteria) {
        if (criteria == null) {
            throw new InvalidSearchCriteriaException("Request must include 'criteria'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        List<String> errors = validateCondition(criteria);
        if (!errors.isEmpty()) {
            throw new InvalidSearchCriteriaException(errors, SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    public static void validateMeta(SearchRequestMeta meta) {
        if (meta == null) {
            return;
        }
        PaginationRequest pagination = meta.getPagination();
        if (pagination == null) {
            return;
        }
        validateLimit(pagination.getLimit());
        validateSortOrder(pagination.getSortOrder());
        validateDirection(pagination.getDirection());
        validateCursor(pagination.getCursor(), pagination.getDirection());
    }

    private static void validateLimit(Integer limit) {
        if (limit != null && limit <= 0) {
            throw new InvalidSearchCriteriaException(
                    "'meta.pagination.limit' must be a positive integer",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private static void validateSortOrder(String sortOrder) {
        if (sortOrder != null && !SUPPORTED_SORT_ORDERS.contains(sortOrder.toLowerCase())) {
            throw new InvalidSearchCriteriaException(
                    "'meta.pagination.sortOrder' must be 'asc' or 'desc'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private static void validateDirection(String direction) {
        if (direction != null && !SUPPORTED_DIRECTIONS.contains(direction.toLowerCase())) {
            throw new InvalidSearchCriteriaException(
                    "'meta.pagination.direction' must be 'next' or 'prev'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private static void validateCursor(String cursor, String direction) {
        if (cursor != null && !cursor.isEmpty()) {
            CursorCodec.decode(cursor);
            if (direction == null || direction.isEmpty()) {
                throw new InvalidSearchCriteriaException(
                        "'meta.pagination.direction' is required when 'cursor' is provided",
                        SearchResponseErrorStatus.BAD_REQUEST);
            }
        }
    }

    private static List<String> validateCondition(SearchCondition condition) {
        if (condition.isLeaf()) {
            return validateLeaf(condition);
        } else if (condition.isGroup()) {
            return validateGroup(condition);
        }
        return Collections.singletonList(
                "Each condition must be either a leaf {field, comparator, value} or a group {operator, conditions}");
    }

    private static List<String> validateLeaf(SearchCondition leaf) {
        List<String> errors = new ArrayList<>();
        if (leaf.getComparator() == null) {
            errors.add("Leaf condition for field '" + leaf.getField()
                    + "' is missing 'comparator'. Supported: eq, gt, lt, ge, le");
        } else if (!SUPPORTED_COMPARATORS.contains(leaf.getComparator())) {
            errors.add("Leaf condition for field '" + leaf.getField()
                    + "' has unsupported 'comparator': '" + leaf.getComparator()
                    + "'. Supported: eq, gt, lt, ge, le");
        }
        if (leaf.getValue() == null || leaf.getValue().isEmpty()) {
            errors.add("Leaf condition for field '" + leaf.getField() + "' is missing 'value'");
        }
        return errors;
    }

    private static List<String> validateGroup(SearchCondition group) {
        if (group.getConditions() == null || group.getConditions().isEmpty()) {
            return Collections.singletonList(
                    "A group condition must have at least one condition in 'conditions'");
        }
        List<String> errors = new ArrayList<>();
        if (group.getOperator() == null) {
            errors.add("Group condition is missing 'operator'. Supported: AND, OR");
        } else if (!SUPPORTED_OPERATORS.contains(group.getOperator())) {
            errors.add("Group condition has unsupported 'operator': '" + group.getOperator()
                    + "'. Supported: AND, OR");
        }
        for (SearchCondition child : group.getConditions()) {
            errors.addAll(validateCondition(child));
        }
        return errors;
    }
}
