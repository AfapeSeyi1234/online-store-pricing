package com.store.pricing;

// How much delivery costs. Different implementations give different pricing.
public interface ShippingCalculator {

    double feeFor(String location, double subtotal);
}