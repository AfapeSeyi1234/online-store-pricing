package com.store.analytics;

import com.store.model.OrderStatus;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SalesReport {

    private SalesReport() {
    }

    public static String create(OrderAnalyser analyser) {
        StringBuilder text = new StringBuilder();
        text.append("===== SALES SUMMARY =====\n");
        text.append("Orders in total: ").append(analyser.orderCount()).append("\n");
        text.append("Orders counted as sales: ").append(analyser.salesOrders().size()).append("\n");
        text.append("Cancelled orders: ")
                .append(analyser.countByStatus().getOrDefault(OrderStatus.CANCELLED, 0L)).append("\n");
        text.append("Total sales: ").append(money(analyser.totalSales())).append("\n");
        text.append("Average order value: ").append(money(analyser.averageOrderValue())).append("\n");
        appendCustomers(text, analyser);
        appendStatusCounts(text, analyser);
        appendSalesByCustomer(text, analyser);
        appendBestSellers(text, analyser);
        return text.toString();
    }

    private static void appendCustomers(StringBuilder text, OrderAnalyser analyser) {
        if (analyser.customersWhoOrdered().isEmpty()) {
            text.append("Customers who ordered: none\n");
        } else {
            text.append("Customers who ordered: ")
                    .append(String.join(", ", analyser.customersWhoOrdered())).append("\n");
        }
    }

    private static void appendStatusCounts(StringBuilder text, OrderAnalyser analyser) {
        Map<OrderStatus, Long> counts = analyser.countByStatus();
        text.append("\nOrders by status:\n");
        for (OrderStatus status : OrderStatus.values()) {
            text.append(String.format(Locale.US, "  %-10s %d", status, counts.getOrDefault(status, 0L)))
                    .append("\n");
        }
    }

    private static void appendSalesByCustomer(StringBuilder text, OrderAnalyser analyser) {
        Map<String, Double> sales = analyser.salesByCustomer();
        text.append("\nSales by customer:\n");
        if (sales.isEmpty()) {
            text.append("  No sales yet.\n");
            return;
        }
        for (Map.Entry<String, Double> entry : sales.entrySet()) {
            text.append(String.format(Locale.US, "  %-12s %s", entry.getKey(), money(entry.getValue())))
                    .append("\n");
        }
    }

    private static void appendBestSellers(StringBuilder text, OrderAnalyser analyser) {
        List<Map.Entry<String, Integer>> best = analyser.bestSellingProducts(3);
        text.append("\nBest-selling products:\n");
        if (best.isEmpty()) {
            text.append("  No sales yet.\n");
            return;
        }
        int rank = 1;
        for (Map.Entry<String, Integer> entry : best) {
            text.append("  ").append(rank).append(". ")
                    .append(entry.getKey()).append(" - ").append(entry.getValue()).append(" units\n");
            rank++;
        }
    }

    private static String money(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }
}