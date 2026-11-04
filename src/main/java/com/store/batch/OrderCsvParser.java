package com.store.batch;

import com.store.model.OrderStatus;
import com.store.validation.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Turns CSV lines into orders. Columns:
// orderId,customerId,productId,quantity,unitPrice,orderDate,createdAt,status
// A bad line never stops the import. It is rejected with every reason found, and the rest carry on.
public final class OrderCsvParser {

    public static final int COLUMN_COUNT = 8;
    private static final String HEADER_START = "orderid,";

    private OrderCsvParser() {
    }

    // "today" is passed in so that tests do not depend on the real date.
    public static ImportResult parse(List<String> lines, LocalDate today) {
        Validator.requireNotNull(lines, "Lines");
        Validator.requireNotNull(today, "Today's date");

        List<ImportedOrder> accepted = new ArrayList<>();
        List<RejectedRecord> rejected = new ArrayList<>();
        Map<String, Integer> firstSeenLine = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = removeByteOrderMark(lines.get(i), i);

            if (line == null || line.isBlank()) {
                continue; // empty lines are ignored
            }
            if (lineNumber == 1 && line.trim().toLowerCase(Locale.ROOT).startsWith(HEADER_START)) {
                continue; // header line
            }

            List<String> problems = new ArrayList<>();
            ImportedOrder order = parseLine(line, today, problems);

            if (order != null && firstSeenLine.containsKey(order.orderId())) {
                problems.add("duplicate orderId " + order.orderId()
                        + " (first seen on line " + firstSeenLine.get(order.orderId()) + ").");
                order = null;
            }

            if (order == null) {
                rejected.add(new RejectedRecord(lineNumber, line, problems));
            } else {
                firstSeenLine.put(order.orderId(), lineNumber);
                accepted.add(order);
            }
        }
        return new ImportResult(accepted, rejected);
    }

    // Returns the order, or null if anything is wrong (the reasons are added to "problems").
    private static ImportedOrder parseLine(String line, LocalDate today, List<String> problems) {
        String[] parts = line.split(",", -1); // -1 keeps empty values at the end
        if (parts.length != COLUMN_COUNT) {
            problems.add("expected " + COLUMN_COUNT + " values but found " + parts.length + ".");
            return null;
        }

        String orderId = required(parts[0], "orderId", problems);
        String customerId = required(parts[1], "customerId", problems);
        String productId = required(parts[2], "productId", problems);
        Integer quantity = parseQuantity(parts[3], problems);
        Double unitPrice = parseUnitPrice(parts[4], problems);
        LocalDate orderDate = parseOrderDate(parts[5], today, problems);
        LocalDateTime createdAt = parseCreatedAt(parts[6], problems);
        OrderStatus status = parseStatus(parts[7], problems);

        if (orderDate != null && createdAt != null && !createdAt.toLocalDate().equals(orderDate)) {
            problems.add("createdAt date does not match orderDate.");
        }

        if (!problems.isEmpty()) {
            return null;
        }
        return new ImportedOrder(orderId, customerId, productId, quantity, unitPrice,
                orderDate, createdAt, status);
    }

    private static String required(String value, String name, List<String> problems) {
        if (value == null || value.isBlank()) {
            problems.add(name + " is required.");
            return null;
        }
        return value.trim();
    }

    private static Integer parseQuantity(String value, List<String> problems) {
        try {
            int quantity = Integer.parseInt(value.trim());
            if (Validator.isValidQuantity(quantity)) {
                return quantity;
            }
        } catch (NumberFormatException e) {
            // falls through to the message below
        }
        problems.add("quantity must be a whole number from 1 to 100.");
        return null;
    }

    private static Double parseUnitPrice(String value, List<String> problems) {
        String text = value.trim();
        try {
            double price = Double.parseDouble(text);
            if (Validator.isValidPrice(price)) {
                return price;
            }
            problems.add("unitPrice must be greater than 0 and at most 100000.");
        } catch (NumberFormatException e) {
            problems.add("unitPrice '" + text + "' is not a number.");
        }
        return null;
    }

    private static LocalDate parseOrderDate(String value, LocalDate today, List<String> problems) {
        String text = value.trim();
        try {
            LocalDate date = LocalDate.parse(text); // expects yyyy-MM-dd and rejects dates like 2026-02-30
            if (date.isAfter(today)) {
                problems.add("orderDate " + date + " is in the future.");
            }
            return date;
        } catch (DateTimeParseException e) {
            problems.add("orderDate '" + text + "' is not a valid date (use yyyy-MM-dd).");
            return null;
        }
    }

    private static LocalDateTime parseCreatedAt(String value, List<String> problems) {
        String text = value.trim();
        try {
            return LocalDateTime.parse(text); // expects yyyy-MM-ddTHH:mm:ss
        } catch (DateTimeParseException e) {
            problems.add("createdAt '" + text + "' is not a valid timestamp (use yyyy-MM-ddTHH:mm:ss).");
            return null;
        }
    }

    private static OrderStatus parseStatus(String value, List<String> problems) {
        String text = value.trim();
        if (text.isEmpty()) {
            problems.add("status is required.");
            return null;
        }
        try {
            return OrderStatus.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            problems.add("status '" + text
                    + "' is not valid (use NEW, PAID, SHIPPED, DELIVERED or CANCELLED).");
            return null;
        }
    }

    // Files saved by Excel can start with an invisible marker character. Remove it.
    private static String removeByteOrderMark(String line, int index) {
        if (index == 0 && line != null && line.startsWith("\uFEFF")) {
            return line.substring(1);
        }
        return line;
    }
}