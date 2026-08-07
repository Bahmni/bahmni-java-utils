package org.bahmni.search.builder;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;
import org.bahmni.search.model.FieldType;
import org.bahmni.search.model.SearchCondition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class AbstractCriteriaBuilder<T> {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    protected abstract Predicate buildLeafCriterion(CriteriaBuilder cb, Root<T> root,
                                                     SearchCondition leaf, Map<String, ?> joinCache);

    public Predicate buildCriterion(CriteriaBuilder cb, Root<T> root,
                                     SearchCondition condition, Map<String, ?> joinCache) {
        if (condition == null) {
            return null;
        }
        if (condition.isLeaf()) {
            return buildLeafCriterion(cb, root, condition, joinCache);
        }
        return combineChildPredicates(cb, root, condition, joinCache);
    }

    protected Predicate combineChildPredicates(CriteriaBuilder cb, Root<T> root,
                                                SearchCondition parent, Map<String, ?> joinCache) {
        List<Predicate> childPredicates = new ArrayList<>();
        if (parent.getConditions() != null) {
            for (SearchCondition child : parent.getConditions()) {
                Predicate p = buildCriterion(cb, root, child, joinCache);
                if (p != null) {
                    childPredicates.add(p);
                }
            }
        }

        if (childPredicates.isEmpty()) {
            return null;
        }
        if (childPredicates.size() == 1) {
            return childPredicates.get(0);
        }

        Predicate[] array = childPredicates.toArray(new Predicate[0]);
        return parent.getOperator() == ConditionOperator.OR
                ? cb.or(array)
                : cb.and(array);
    }

    @SuppressWarnings("unchecked")
    protected Predicate buildPredicate(CriteriaBuilder cb, Path<?> fieldPath,
                                       FieldComparator comparator, String value) {
        switch (comparator) {
            case EQ:
                return cb.equal(fieldPath, value);
            case GT:
                return cb.greaterThan((Path<Date>) fieldPath, parseDate(value));
            case LT:
                return cb.lessThan((Path<Date>) fieldPath, parseDate(value));
            case GE:
                return cb.greaterThanOrEqualTo((Path<Date>) fieldPath, parseDate(value));
            case LE:
                return cb.lessThanOrEqualTo((Path<Date>) fieldPath, parseDate(value));
            default:
                throw new InvalidSearchCriteriaException(
                        "Unsupported comparator: " + comparator,
                        SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    protected void validateComparator(String fieldName, FieldComparator comparator, FieldType fieldType) {
        if (!fieldType.supports(comparator)) {
            throw new InvalidSearchCriteriaException(
                    "Comparator '" + comparator.name().toLowerCase()
                            + "' is not supported for field '" + fieldName
                            + "'. Supported: " + fieldType.getSupportedComparators().toString().toLowerCase(),
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    protected Date parseDate(String dateValue) {
        try {
            return Date.from(OffsetDateTime.parse(dateValue, ISO_DATETIME_FORMAT).toInstant());
        } catch (DateTimeParseException exception) {
            throw new InvalidSearchCriteriaException(
                    "Invalid date format: '" + dateValue
                            + "'. Expected yyyy-MM-dd'T'HH:mm:ss.SSSZ (e.g. 2024-01-01T10:30:00.000+0530)",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
