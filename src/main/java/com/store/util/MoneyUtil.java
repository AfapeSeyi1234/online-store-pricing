package com.store.util;

public final class MoneyUtil {

    private MoneyUtil() {
    }

    // Rounds to 2 decimal places, like money.
    public static double round2(double value) {
        return Math.round(value * 100) / 100.0;
    }
}