package com.store.analytics;

import com.store.exception.ValidationException;
import com.store.model.Order;
import com.store.model.OrderItem;
import com.store.model.OrderStatus;
import com.store.util.CollectionUtil;
import com.store.util.MoneyUtil;
import com.store.validation.Validator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class OrderAnalyser {

    private final List<Order> orders;

    public OrderAnalyser(List<Order> orders) {
        Validator.requireNotNull(orders, "Orders");
        this.orders = List.copyOf(orders);
    }

    public int orderCount() {
        return orders.size();
    }

    // An order counts as a sale once it is paid. New and cancelled orders do not.
    public static boolean isSale(Order order) {
        OrderStatus status = order.getStatus();
        return status == OrderStatus.PAID
                || status == OrderStatus.SHIPPED
                || status == OrderStatus.DELIVERED;
    }

    // ---------- search ----------

    // Finds orders whose id or customer name contains the text.
    public List<Order> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String needle = text.trim().toLowerCase();
        return orders.stream()
                .filter(o -> o.getId().toLowerCase().contains(needle)
                        || o.getCustomer().getName().toLowerCase().contains(needle))
                .toList();
    }

    // ---------- filters ----------

    public List<Order> filterByStatus(OrderStatus status) {
        return CollectionUtil.filter(orders, o -> o.getStatus() == status);
    }

    public List<Order> filterByCustomer(String customerId) {
        return CollectionUtil.filter(orders, o -> o.getCustomer().getId().equals(customerId));
    }

    // Both the first and the last day are included.
    public List<Order> filterByDate(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new ValidationException("Date range is not valid.");
        }
        return CollectionUtil.filter(orders,
                o -> !o.getCreatedOn().isBefore(from) && !o.getCreatedOn().isAfter(to));
    }

    // Both the minimum and the maximum value are included.
    public List<Order> filterByValue(double min, double max) {
        if (min < 0 || max < min) {
            throw new ValidationException("Value range is not valid.");
        }
        return CollectionUtil.filter(orders, o -> o.getTotal() >= min && o.getTotal() <= max);
    }

    // ---------- grouping ----------

    public Map<OrderStatus, List<Order>> groupByStatus() {
        return CollectionUtil.groupBy(orders, Order::getStatus);
    }

    public Map<String, List<Order>> groupByCustomer() {
        return CollectionUtil.groupBy(orders, o -> o.getCustomer().getId());
    }

    public Map<OrderStatus, Long> countByStatus() {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    // ---------- duplicates ----------

    public Set<String> findDuplicateOrderIds() {
        return CollectionUtil.findDuplicateKeys(orders, Order::getId);
    }

    public List<Order> removeDuplicateOrders() {
        return CollectionUtil.removeDuplicates(orders, Order::getId);
    }

    // ---------- sales figures ----------

    public List<Order> salesOrders() {
        return CollectionUtil.filter(orders, OrderAnalyser::isSale);
    }

    public double totalSales() {
        double total = salesOrders().stream().mapToDouble(Order::getTotal).sum();
        return MoneyUtil.round2(total);
    }

    // Returns 0 when there are no sales (average() gives an empty result for an empty stream).
    public double averageOrderValue() {
        double average = salesOrders().stream().mapToDouble(Order::getTotal).average().orElse(0);
        return MoneyUtil.round2(average);
    }

    // Customer names, each once, in alphabetical order. A TreeSet is a sorted Set.
    public Set<String> customersWhoOrdered() {
        Set<String> names = new TreeSet<>();
        for (Order order : orders) {
            names.add(order.getCustomer().getName());
        }
        return names;
    }

    public Map<String, Double> salesByCustomer() {
        return new TreeMap<>(salesOrders().stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCustomer().getName(),
                        Collectors.summingDouble(Order::getTotal))));
    }

    // How many units of each product were sold (sales orders only).
    public Map<String, Integer> unitsSoldByProduct() {
        Map<String, Integer> units = new HashMap<>();
        for (Order order : salesOrders()) {
            for (OrderItem item : order.getItems()) {
                units.merge(item.product().getName(), item.quantity(), Integer::sum);
            }
        }
        return units;
    }

    // Most units first. If two products tie, they are sorted by name.
    public List<Map.Entry<String, Integer>> bestSellingProducts(int limit) {
        if (limit < 1) {
            throw new ValidationException("Limit must be at least 1.");
        }
        return unitsSoldByProduct().entrySet().stream()
                .sorted((a, b) -> {
                    int byUnits = Integer.compare(b.getValue(), a.getValue());
                    if (byUnits != 0) {
                        return byUnits;
                    }
                    return a.getKey().compareTo(b.getKey());
                })
                .limit(limit)
                .toList();
    }
}