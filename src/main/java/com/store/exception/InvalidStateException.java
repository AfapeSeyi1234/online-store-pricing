package com.store.exception;

// Thrown when an action is not allowed in the current state (e.g. shipping an unpaid order).
public class InvalidStateException extends IllegalStateException {

    public InvalidStateException(String message) {
        super(message);
    }
}