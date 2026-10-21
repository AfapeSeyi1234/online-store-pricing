package com.store.exception;

public class DuplicateOrderException extends ValidationException {

    public DuplicateOrderException(String orderId) {
        super("Order " + orderId + " already exists.");
    }
}