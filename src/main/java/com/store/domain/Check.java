package com.store.domain;

// Small helper so every class checks text the same way.
final class Check {

    private Check() {
    }

    static String text(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}