package com.store.pricing;

public class BulkDiscountPolicy implements DiscountPolicy {

    private static final int BULK_UNITS = 10;
    private static final double BULK_PERCENT = 3;

    @Override
    public double percentFor(double subtotal, int totalUnits, boolean isMember) {
        if (totalUnits >= BULK_UNITS) {
            return BULK_PERCENT;
        }
        return 0;
    }
}