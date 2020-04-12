package com.joba.hadoop.commons.validation;

/**
 * Predicate class to be used in Validations
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public class Predicate {

    private static final class ValidationException extends RuntimeException {

        private ValidationException() {
        }

        private ValidationException(String message) {
            super(message);
        }
    }

    private final Condition condition;
    private final String errorMessage;
    private final Predicate[] toEvaluateAfter;

    Predicate(Predicate... toEvaluateAfter) {
        this(null, null, toEvaluateAfter);
    }

    Predicate(Condition condition, String errorMessage, Predicate... toEvaluateAfter) {
        this.condition = condition;
        this.errorMessage = errorMessage;
        Predicate.checkState(condition == null || errorMessage != null, "error message cannot be null");
        this.toEvaluateAfter = toEvaluateAfter;
    }

    String evaluate() {
        if (condition != null) {
            if (!condition.evaluate()) {
                return errorMessage;
            }
        }

        if (toEvaluateAfter != null) {
            for (Predicate predicate : toEvaluateAfter) {
                String result = predicate.evaluate();
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static void checkState(boolean expression, String errorMessage) {
        if (!expression) {
            throw new ValidationException(errorMessage);
        }
    }
}