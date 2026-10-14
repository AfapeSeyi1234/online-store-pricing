package com.store.pricing;

// One discount rule. Each rule says how many percent it gives for an order.
public interface DiscountPolicy {

    double percentFor(double subtotal, int totalUnits, boolean isMember);
}