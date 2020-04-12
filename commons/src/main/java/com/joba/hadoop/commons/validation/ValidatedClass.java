package com.joba.hadoop.commons.validation;

import com.joba.hadoop.commons.Byteable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Interface that a class must follow to be able to auto validate parameters when created
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public abstract class ValidatedClass extends Byteable {
    protected abstract List<Predicate> commonValidation();

    public final void validate() {
        try {
            validate(commonValidation());
        } catch (Exception e) {
            throw new PredicateCheckException(e.getMessage(), Collections.singletonList(e.getMessage()));
        }
    }

    public final void validate(List<Predicate> predicates) {
        List<String> errorMessages = new LinkedList<>();
        checkPredicates(commonValidation(), errorMessages);
        if (predicates != null) {
            checkPredicates(predicates, errorMessages);
        }

        if (!errorMessages.isEmpty()) {
            throw new PredicateCheckException("Predicate check failed: " + errorMessages.toString(), errorMessages);
        }
    }

    private void checkPredicates(List<Predicate> predicates, List<String> errorMessages) {
        for (Predicate predicate : predicates) {
            String error = predicate.evaluate();
            if (error != null) {
                errorMessages.add(error);
            }
        }
    }
}
