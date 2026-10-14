package com.store.validation;

public final class OrderInputValidator {

    public static final int MAX_LINES = 20;

    private OrderInputValidator() {
    }

    // Returns an error message, or null if everything is fine.
    public static String validate(double[] prices, int[] quantities, String location) {
        String shapeError = checkShape(prices, quantities);
        if (shapeError != null) {
            return shapeError;
        }
        for (int i = 0; i < prices.length; i++) {
            String lineError = checkLine(i + 1, prices[i], quantities[i]);
            if (lineError != null) {
                return lineError;
            }
        }
        if (!Validator.isValidLocation(location)) {
            return "Location must be local, regional or international.";
        }
        return null;
    }

    private static String checkShape(double[] prices, int[] quantities) {
        if (prices == null || quantities == null || prices.length == 0) {
            return "Order must contain at least one item.";
        }
        if (prices.length != quantities.length) {
            return "Prices and quantities must have the same length.";
        }
        if (prices.length > MAX_LINES) {
            return "Order cannot have more than " + MAX_LINES + " items.";
        }
        return null;
    }

    private static String checkLine(int itemNumber, double price, int quantity) {
        if (!Validator.isValidPrice(price)) {
            return "Item " + itemNumber + ": price must be greater than 0 and at most 100000.";
        }
        if (!Validator.isValidQuantity(quantity)) {
            return "Item " + itemNumber + ": quantity must be between 1 and 100.";
        }
        return null;
    }
}