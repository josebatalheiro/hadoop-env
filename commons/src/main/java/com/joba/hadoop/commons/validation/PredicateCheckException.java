package com.joba.hadoop.commons.validation;

import java.util.List;

/**
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public class PredicateCheckException extends RuntimeException {

    private final List<String> errorMessages;

    PredicateCheckException(String message, List<String> errorMessages) {
        super(message);
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}