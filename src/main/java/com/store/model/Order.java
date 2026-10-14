package com.store.model;

import com.store.OrderCalculator;
import com.store.pricing.OrderResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    public static final int MAX_ITEMS = 20;

    private final String id;
    private final Customer customer;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.NEW;
    private Payment payment;     // empty until pay() is called
    private Delivery delivery;   // empty until ship() is called

    public Order(String id, Customer customer) {
        this.id = Check.text(id, "Order id");
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        this.customer = customer;
    }

    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Payment getPayment() {
        return payment;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    // Outsiders get a read-only view, so they cannot change the list directly.
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.NEW) {
            throw new IllegalStateException("Items can only be added to a new order.");
        }
        if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            throw new IllegalStateException("A payment is in progress. Items cannot be added now.");
        }
        if (items.size() >= MAX_ITEMS) {
            throw new IllegalStateException("An order cannot have more than " + MAX_ITEMS + " items.");
        }
        OrderItem item = OrderItem.of(product, quantity); // validates the product and quantity
        product.reduceStock(quantity);                    // validates the stock
        items.add(item);                                  // only reached if both checks passed
    }

    // Uses the Week 1 pricing rules.
    public OrderResult calculatePricing() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Order has no items.");
        }
        double[] prices = new double[items.size()];
        int[] quantities = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            prices[i] = items.get(i).unitPrice();
            quantities[i] = items.get(i).quantity();
        }
        return OrderCalculator.calculate(prices, quantities,
                customer.getAddress().zone(), customer.isLoyaltyMember());
    }

    public void pay(PaymentMethod method) {
        if (status != OrderStatus.NEW) {
            throw new IllegalStateException("Only a new order can be paid.");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot pay for an empty order.");
        }
        if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            throw new IllegalStateException("A payment is already in progress.");
        }
        payment = new Payment(calculatePricing().total, method);
    }

    public void confirmPayment() {
        if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("There is no pending payment to confirm.");
        }
        payment.markSuccessful();
        moveTo(OrderStatus.PAID);
    }

    public void failPayment() {
        if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("There is no pending payment to fail.");
        }
        payment.markFailed(); // the order stays NEW, so the customer can try again
    }

    public void ship() {
        moveTo(OrderStatus.SHIPPED);
        delivery = new Delivery(customer.getAddress());
        delivery.markInTransit();
    }

    public void confirmDelivery() {
        moveTo(OrderStatus.DELIVERED);
        delivery.markDelivered();
    }

    public void cancel() {
        moveTo(OrderStatus.CANCELLED);
        for (OrderItem item : items) {
            item.product().addStock(item.quantity()); // give the stock back
        }
        if (payment != null) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.markFailed();
            } else if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
                payment.refund();
            }
        }
    }

    // The only place the status changes, so every change goes through the rules.
    private void moveTo(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException("Cannot change order from " + status + " to " + next + ".");
        }
        status = next;
    }
}