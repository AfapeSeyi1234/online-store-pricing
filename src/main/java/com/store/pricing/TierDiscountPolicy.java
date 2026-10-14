package com.store.pricing;

public class TierDiscountPolicy implements DiscountPolicy {

    @Override
    public double percentFor(double subtotal, int totalUnits, boolean isMember) {
        if (subtotal >= 100) {
            return 10;
        }
        if (subtotal >= 50) {
            return 5;
        }
        return 0;
    }
}