package com.store;

/**
 * Business logic only. No Scanner, no System.out here.
 *
 * RULES
 * 1. Subtotal >= 100 -> 10% discount, subtotal >= 50 -> 5% discount.
 * 2. 10 or more items in total -> extra 3% discount.
 * 3. Loyalty member -> extra 5% discount.
 *    Discounts add up, but the total discount is never more than 15%.
 * 4. Delivery: local 5, regional 12, international 30.
 *    Free delivery if subtotal >= 80 (local) or >= 200 (regional).
 *    International is never free.
 * 5. Tax is 7.5% of the discounted subtotal (delivery is not taxed).
 * 6. Total = discounted subtotal + delivery + tax.
 */
public class OrderCalculator {

    public static final double TAX_RATE = 0.075;
    public static final double MAX_DISCOUNT_PERCENT = 15;

    // Returns an error message, or null if everything is fine.
    public static String validate(double[] prices, int[] quantities, String location) {
        if (prices == null || quantities == null || prices.length == 0) {
            return "Order must contain at least one item.";
        }
        if (prices.length != quantities.length) {
            return "Prices and quantities must have the same length.";
        }
        if (prices.length > 20) {
            return "Order cannot have more than 20 items.";
        }

        for (int i = 0; i < prices.length; i++) {
            int itemNumber = i + 1;

            boolean priceOk = prices[i] > 0 && prices[i] <= 100000;
            if (!priceOk) {
                return "Item " + itemNumber + ": price must be greater than 0 and at most 100000.";
            }

            boolean quantityOk = quantities[i] >= 1 && quantities[i] <= 100;
            if (!quantityOk) {
                return "Item " + itemNumber + ": quantity must be between 1 and 100.";
            }
        }

        if (!isValidLocation(location)) {
            return "Location must be local, regional or international.";
        }
        return null;
    }

    public static OrderResult calculate(double[] prices, int[] quantities,
                                        String location, boolean isMember) {
        String error = validate(prices, quantities, location);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        double subtotal = 0;
        int totalUnits = 0;
        for (int i = 0; i < prices.length; i++) {
            subtotal = subtotal + prices[i] * quantities[i];
            totalUnits = totalUnits + quantities[i];
        }
        subtotal = round2(subtotal);

        double discountPercent = getDiscountPercent(subtotal, totalUnits, isMember);
        double discount = round2(subtotal * discountPercent / 100);
        double discountedSubtotal = round2(subtotal - discount);
        double deliveryFee = getDeliveryFee(location, subtotal);
        double tax = round2(discountedSubtotal * TAX_RATE);
        double total = round2(discountedSubtotal + deliveryFee + tax);

        OrderResult result = new OrderResult();
        result.subtotal = subtotal;
        result.discountPercent = discountPercent;
        result.discount = discount;
        result.discountedSubtotal = discountedSubtotal;
        result.deliveryFee = deliveryFee;
        result.tax = tax;
        result.total = total;
        return result;
    }

    public static double getDiscountPercent(double subtotal, int totalUnits, boolean isMember) {
        double percent = 0;

        // Rule 1: order value
        if (subtotal >= 100) {
            percent = percent + 10;
        } else if (subtotal >= 50) {
            percent = percent + 5;
        }

        // Rule 2: bulk
        if (totalUnits >= 10) {
            percent = percent + 3;
        }

        // Rule 3: member
        if (isMember) {
            percent = percent + 5;
        }

        // Cap
        if (percent > MAX_DISCOUNT_PERCENT) {
            percent = MAX_DISCOUNT_PERCENT;
        }
        return percent;
    }

    public static double getDeliveryFee(String location, double subtotal) {
        if (location.equalsIgnoreCase("local")) {
            if (subtotal >= 80) {
                return 0;
            }
            return 5;
        } else if (location.equalsIgnoreCase("regional")) {
            if (subtotal >= 200) {
                return 0;
            }
            return 12;
        } else {
            return 30; // international, never free
        }
    }

    public static boolean isValidLocation(String location) {
        if (location == null) {
            return false;
        }
        return location.equalsIgnoreCase("local")
                || location.equalsIgnoreCase("regional")
                || location.equalsIgnoreCase("international");
    }

    // Rounds to 2 decimal places, like money.
    public static double round2(double value) {
        return Math.round(value * 100) / 100.0;
    }
}