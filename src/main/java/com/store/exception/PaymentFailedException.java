package com.store.exception;

// Unchecked: a declined payment is an expected business outcome.
public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }
}