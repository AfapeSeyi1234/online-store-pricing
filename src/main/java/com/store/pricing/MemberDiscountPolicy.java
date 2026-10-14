package com.store.pricing;

public class MemberDiscountPolicy implements DiscountPolicy {

    @Override
    public double percentFor(double subtotal, int totalUnits, boolean isMember) {
        if (isMember) {
            return 5;
        }
        return 0;
    }
}