package com.store.app;

import com.store.analytics.LoadResult;
import com.store.analytics.OrderAnalyser;
import com.store.analytics.ProductAnalyser;
import com.store.analytics.ProductCsvLoader;
import com.store.analytics.SalesReport;
import com.store.analytics.SampleData;
import com.store.exception.ValidationException;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.OrderStatus;
import com.store.model.Product;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

// Only talks to the user. All the work is done by the analyser classes.
public class ReportApp {

    public static void main(String[] args) {
        SampleData data = SampleData.create();
        ProductAnalyser products = new ProductAnalyser(data.getProducts());
        OrderAnalyser orders = new OrderAnalyser(data.getOrders());
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt(scanner, "Choose an option: ", 0, 10);
            running = handle(choice, scanner, data, products, orders);
        }
        System.out.println("Goodbye.");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Order and Sales Analyser =====");
        System.out.println(" 1. Search products");
        System.out.println(" 2. Search orders");
        System.out.println(" 3. Sort products");
        System.out.println(" 4. Filter orders by status");
        System.out.println(" 5. Filter orders by customer");
        System.out.println(" 6. Filter orders by date range");
        System.out.println(" 7. Filter orders by value range");
        System.out.println(" 8. Group orders");
        System.out.println(" 9. Check product data for duplicates and invalid records");
        System.out.println("10. Sales summary");
        System.out.println(" 0. Exit");
    }

    private static boolean handle(int choice, Scanner scanner, SampleData data,
                                  ProductAnalyser products, OrderAnalyser orders) {
        if (choice == 0) {
            return false;
        }
        try {
            switch (choice) {
                case 1 -> printProducts(products.search(readText(scanner, "Search text: ")));
                case 2 -> printOrders(orders.search(readText(scanner, "Search text (order id or customer): ")));
                case 3 -> sortProducts(scanner, products);
                case 4 -> filterByStatus(scanner, orders);
                case 5 -> filterByCustomer(scanner, data, orders);
                case 6 -> filterByDate(scanner, orders);
                case 7 -> filterByValue(scanner, orders);
                case 8 -> groupOrders(scanner, orders);
                case 9 -> checkProductData();
                case 10 -> System.out.println(SalesReport.create(orders));
                default -> System.out.println("Unknown option.");
            }
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    // ---------- menu actions ----------

    private static void sortProducts(Scanner scanner, ProductAnalyser products) {
        System.out.println("1. Name   2. Price (low to high)   3. Price (high to low)");
        System.out.println("4. Stock (lowest first)   5. Stock (highest first)");
        int choice = readInt(scanner, "Sort by: ", 1, 5);
        List<Product> sorted = switch (choice) {
            case 1 -> products.sortByName();
            case 2 -> products.sortByPrice(true);
            case 3 -> products.sortByPrice(false);
            case 4 -> products.sortByStock(true);
            default -> products.sortByStock(false);
        };
        printProducts(sorted);
    }

    private static void filterByStatus(Scanner scanner, OrderAnalyser orders) {
        OrderStatus[] statuses = OrderStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        int choice = readInt(scanner, "Status: ", 1, statuses.length);
        printOrders(orders.filterByStatus(statuses[choice - 1]));
    }

    private static void filterByCustomer(Scanner scanner, SampleData data, OrderAnalyser orders) {
        for (Customer customer : data.getCustomers()) {
            System.out.println(customer.getId() + " - " + customer.getName());
        }
        String id = readText(scanner, "Customer id: ").trim().toUpperCase();
        printOrders(orders.filterByCustomer(id));
    }

    private static void filterByDate(Scanner scanner, OrderAnalyser orders) {
        LocalDate from = readDate(scanner, "From date (yyyy-mm-dd): ");
        LocalDate to = readDate(scanner, "To date (yyyy-mm-dd): ");
        printOrders(orders.filterByDate(from, to));
    }

    private static void filterByValue(Scanner scanner, OrderAnalyser orders) {
        double min = readDouble(scanner, "Minimum value: ");
        double max = readDouble(scanner, "Maximum value: ");
        printOrders(orders.filterByValue(min, max));
    }

    private static void groupOrders(Scanner scanner, OrderAnalyser orders) {
        System.out.println("1. By customer   2. By status");
        int choice = readInt(scanner, "Group by: ", 1, 2);
        if (choice == 1) {
            for (Map.Entry<String, List<Order>> group : orders.groupByCustomer().entrySet()) {
                System.out.println(group.getKey() + ": " + orderIds(group.getValue()));
            }
        } else {
            for (Map.Entry<OrderStatus, List<Order>> group : orders.groupByStatus().entrySet()) {
                System.out.println(group.getKey() + ": " + orderIds(group.getValue()));
            }
        }
    }

    private static void checkProductData() {
        LoadResult<Product> result = ProductCsvLoader.load(SampleData.productCsvLines());
        System.out.println("Loaded " + result.items().size() + " valid products.");
        if (result.hasErrors()) {
            System.out.println("Skipped " + result.errors().size() + " bad record(s):");
            for (String error : result.errors()) {
                System.out.println(" - " + error);
            }
        }
    }

    // ---------- printing ----------

    private static void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        for (Product p : products) {
            System.out.printf("%-4s %-12s %8.2f   stock: %d%n",
                    p.getId(), p.getName(), p.getPrice(), p.getStock());
        }
    }

    private static void printOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        for (Order o : orders) {
            System.out.printf("%-4s %-12s %s %-10s %9.2f%n",
                    o.getId(), o.getCustomer().getName(), o.getCreatedOn(), o.getStatus(), o.getTotal());
        }
    }

    private static String orderIds(List<Order> orders) {
        return orders.stream().map(Order::getId).collect(Collectors.joining(", "));
    }

    // ---------- input helpers: keep asking until the answer is valid ----------

    private static String readText(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private static int readInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            String text = readText(scanner, message).trim();
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

    private static double readDouble(Scanner scanner, String message) {
        while (true) {
            String text = readText(scanner, message).trim();
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                System.out.println("'" + text + "' is not a number. Try again.");
            }
        }
    }

    private static LocalDate readDate(Scanner scanner, String message) {
        while (true) {
            String text = readText(scanner, message).trim();
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                System.out.println("'" + text + "' is not a date. Use the format 2026-10-05.");
            }
        }
    }
}