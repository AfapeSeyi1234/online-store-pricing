package com.store.exception;

// Thrown when data breaks a rule. Extends IllegalArgumentException so old code and tests still work.
public class ValidationException extends IllegalArgumentException {

    public ValidationException(String message) {
        super(message);
    }
}