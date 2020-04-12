package com.joba.hadoop.commons.validation;

/**
 * Validation class that returns common Predicates, or custom ones
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public class Validation {

    public static Predicate isNull(final Object reference, String fieldName) {
        return new Predicate(() -> (reference == null), "'" + fieldName.trim() + "' has to be null");
    }

    public static Predicate notNull(final Object reference, String fieldName, String messagePrefix) {
        return new Predicate(() -> (reference != null), ((messagePrefix != null && !messagePrefix.isEmpty()) ? messagePrefix + " " : "") +
                "'" + fieldName.trim() + "' cannot be null");
    }

    public static Predicate notNull(final Object reference, String fieldName) {
        return new Predicate(() -> (reference != null), "'" + fieldName.trim() + "' cannot be null");
    }

    public static Predicate notNullOrEmpty(final String reference, String fieldName, String messagePrefix) {
        return new Predicate(notNull(reference, fieldName, messagePrefix), new Predicate(() -> !reference.isEmpty(), ((messagePrefix != null && !messagePrefix.isEmpty()) ? messagePrefix + " " : "") +
                "'" + fieldName.trim() + "' cannot be empty"));
    }

    public static Predicate notNullOrEmpty(final String reference, String fieldName) {
        return notNullOrEmpty(reference, fieldName, null);
    }

    public static Predicate notNullAndState(final Object reference, Condition stateCondition,
                                            String fieldName, String stateErrorSuffix) {
        return new Predicate(notNull(reference, fieldName),
                new Predicate(stateCondition, "'" + fieldName.trim() + "' " + stateErrorSuffix));
    }

    public static Predicate state(final boolean expression, String fieldName, String stateErrorSuffix) {
        return new Predicate(() -> expression, "'" + fieldName.trim() + "' " + stateErrorSuffix);
    }
}