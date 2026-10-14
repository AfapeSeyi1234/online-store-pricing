package com.store.pricing;

// The Week 1 rules: fee depends on the zone, and big orders ship free.
public class ZoneShippingCalculator implements ShippingCalculator {

    private static final double LOCAL_FEE = 5;
    private static final double LOCAL_FREE_FROM = 80;
    private static final double REGIONAL_FEE = 12;
    private static final double REGIONAL_FREE_FROM = 200;
    private static final double INTERNATIONAL_FEE = 30; // never free

    @Override
    public double feeFor(String location, double subtotal) {
        if (location.equalsIgnoreCase("local")) {
            return feeOrFree(subtotal, LOCAL_FREE_FROM, LOCAL_FEE);
        }
        if (location.equalsIgnoreCase("regional")) {
            return feeOrFree(subtotal, REGIONAL_FREE_FROM, REGIONAL_FEE);
        }
        return INTERNATIONAL_FEE;
    }

    private double feeOrFree(double subtotal, double freeFrom, double fee) {
        if (subtotal >= freeFrom) {
            return 0;
        }
        return fee;
    }
}