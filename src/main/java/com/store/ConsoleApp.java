package com.store;

import java.util.Scanner;

// Only talks to the user. All calculations are done by OrderCalculator.
public class ConsoleApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Online Store Order Calculator ===");

        int itemCount = readInt(scanner, "How many different items? (1-20): ", 1, 20);
        double[] prices = new double[itemCount];
        int[] quantities = new int[itemCount];

        for (int i = 0; i < itemCount; i++) {
            prices[i] = readPrice(scanner, "Item " + (i + 1) + " unit price: ");
            quantities[i] = readInt(scanner, "Item " + (i + 1) + " quantity (1-100): ", 1, 100);
        }

        String location = readLocation(scanner);
        boolean isMember = readYesNo(scanner, "Loyalty member? (y/n): ");

        try {
            OrderResult result = OrderCalculator.calculate(prices, quantities, location, isMember);
            printReceipt(result, location);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printReceipt(OrderResult r, String location) {
        System.out.println();
        System.out.println("---------- RECEIPT ----------");
        System.out.printf("Subtotal:          %10.2f%n", r.subtotal);
        System.out.printf("Discount (%.1f%%):  %10.2f%n", r.discountPercent, -r.discount);
        System.out.printf("After discount:    %10.2f%n", r.discountedSubtotal);
        System.out.printf("Delivery (%s): %10.2f%n", location, r.deliveryFee);
        System.out.printf("Tax (7.5%%):        %10.2f%n", r.tax);
        System.out.println("-----------------------------");
        System.out.printf("FINAL AMOUNT:      %10.2f%n", r.total);
    }

    // ---- helper methods: keep asking until the answer is valid ----

    private static int readInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String text = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(text);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("Please enter a whole number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("'" + text + "' is not a whole number. Try again.");
            }
        }
    }

    private static double readPrice(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String text = scanner.nextLine().trim();
            try {
                double price = Double.parseDouble(text);
                if (price > 0 && price <= 100000) {
                    return price;
                }
                System.out.println("Price must be greater than 0 and at most 100000.");
            } catch (NumberFormatException e) {
                System.out.println("'" + text + "' is not a valid price. Example: 19.99");
            }
        }
    }

    private static String readLocation(Scanner scanner) {
        while (true) {
            System.out.print("Delivery location (local / regional / international): ");
            String text = scanner.nextLine().trim();
            if (OrderCalculator.isValidLocation(text)) {
                return text.toLowerCase();
            }
            System.out.println("Please type local, regional or international.");
        }
    }

    private static boolean readYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String text = scanner.nextLine().trim().toLowerCase();
            if (text.equals("y") || text.equals("yes")) {
                return true;
            }
            if (text.equals("n") || text.equals("no")) {
                return false;
            }
            System.out.println("Please answer y or n.");
        }
    }
}