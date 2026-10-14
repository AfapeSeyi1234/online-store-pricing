package com.store.pricing;

public class BulkDiscountPolicy implements DiscountPolicy {

    private static final int BULK_UNITS = 10;

    @Override
    public double percentFor(double subtotal, int totalUnits, boolean isMember) {
        if (totalUnits >= BULK_UNITS) {
            return 3;
        }
        return 0;
    }
}