package com.store.analytics;

import com.store.exception.ValidationException;
import com.store.model.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Turns lines like "P1,Notebook,50.00,100" into products.
// Bad lines are skipped and reported, so one bad record does not stop the rest.
public final class ProductCsvLoader {

    private ProductCsvLoader() {
    }

    public static LoadResult<Product> load(List<String> lines) {
        List<Product> products = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);

            if (line == null || line.isBlank()) {
                continue; // empty lines are ignored
            }
            if (lineNumber == 1 && line.trim().toLowerCase().startsWith("id,")) {
                continue; // header line
            }

            try {
                Product product = parseLine(line);
                if (seenIds.add(product.getId())) {
                    products.add(product);
                } else {
                    errors.add("Line " + lineNumber + ": duplicate product id " + product.getId() + ".");
                }
            } catch (IllegalArgumentException e) {
                // ValidationException and NumberFormatException are both IllegalArgumentExceptions
                errors.add("Line " + lineNumber + ": " + e.getMessage());
            }
        }
        return new LoadResult<>(products, errors);
    }

    private static Product parseLine(String line) {
        String[] parts = line.split(",", -1); // -1 keeps empty values at the end
        if (parts.length != 4) {
            throw new ValidationException(
                    "expected 4 values (id, name, price, stock) but found " + parts.length + ".");
        }
        String id = parts[0].trim();
        String name = parts[1].trim();
        double price = parseNumber(parts[2]);
        int stock = parseWholeNumber(parts[3]);
        return new Product(id, name, price, stock);
    }

    private static double parseNumber(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("price '" + text.trim() + "' is not a number.");
        }
    }

    private static int parseWholeNumber(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("stock '" + text.trim() + "' is not a whole number.");
        }
    }
}