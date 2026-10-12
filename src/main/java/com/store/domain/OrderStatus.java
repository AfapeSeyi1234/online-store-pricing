package com.store.domain;

public enum OrderStatus {
    NEW, PAID, SHIPPED, DELIVERED, CANCELLED;

    // The rules for which status can follow which.
    public boolean canMoveTo(OrderStatus next) {
        if (this == NEW) {
            return next == PAID || next == CANCELLED;
        }
        if (this == PAID) {
            return next == SHIPPED || next == CANCELLED;
        }
        if (this == SHIPPED) {
            return next == DELIVERED;
        }
        return false; // DELIVERED and CANCELLED are final
    }
}