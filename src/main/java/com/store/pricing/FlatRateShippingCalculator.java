package com.store.pricing;

import com.store.exception.ValidationException;

// Same delivery fee everywhere.
public class FlatRateShippingCalculator implements ShippingCalculator {

    private final double fee;

    public FlatRateShippingCalculator(double fee) {
        if (fee < 0) {
            throw new ValidationException("Shipping fee cannot be negative.");
        }
        this.fee = fee;
    }

    @Override
    public double feeFor(String location, double subtotal) {
        return fee;
    }
}