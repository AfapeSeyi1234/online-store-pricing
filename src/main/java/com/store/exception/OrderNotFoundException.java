package com.store.exception;

// Unchecked: asking for an order that does not exist is a problem the caller can avoid.
public class OrderNotFoundException extends NotFoundException {

    private final String orderId;

    public OrderNotFoundException(String orderId) {
        super("Order " + orderId + " was not found.");
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}