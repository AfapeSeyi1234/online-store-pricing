package com.store.exception;

// For actions the order's current status does not allow (for example paying twice).
public class InvalidOrderStateException extends InvalidStateException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}