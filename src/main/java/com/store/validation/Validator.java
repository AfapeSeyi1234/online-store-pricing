package com.store.validation;

import com.store.exception.ValidationException;

// One home for the validation rules used by many classes.
public final class Validator {

    public static final double MAX_PRICE = 100000;
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 100;

    private Validator() {
    }

    public static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(label + " is required.");
        }
        return value.trim();
    }

    public static <T> T requireNotNull(T value, String label) {
        if (value == null) {
            throw new ValidationException(label + " is required.");
        }
        return value;
    }

    public static String requireEmail(String email) {
        String text = requireText(email, "Email");
        if (!text.contains("@") || !text.contains(".")) {
            throw new ValidationException("Email is not valid.");
        }
        return text;
    }

    public static boolean isValidPrice(double price) {
        return price > 0 && price <= MAX_PRICE;
    }

    public static boolean isValidQuantity(int quantity) {
        return quantity >= MIN_QUANTITY && quantity <= MAX_QUANTITY;
    }

    public static boolean isValidLocation(String location) {
        if (location == null) {
            return false;
        }
        return location.equalsIgnoreCase("local")
                || location.equalsIgnoreCase("regional")
                || location.equalsIgnoreCase("international");
    }
}