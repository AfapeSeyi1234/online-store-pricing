package com.store.model;

import com.store.exception.InvalidStateException;
import com.store.pricing.OrderResult;
import com.store.pricing.PricingService;
import com.store.validation.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    public static final int MAX_ITEMS = 20;

    private final String id;
    private final Customer customer;
    private final PricingService pricing;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.NEW;
    private Payment payment;     // empty until pay() is called
    private Delivery delivery;   // empty until ship() is called

    public Order(String id, Customer customer) {
        this(id, customer, PricingService.standard());
    }

    public Order(String id, Customer customer, PricingService pricing) {
        this.id = Validator.requireText(id, "Order id");
        this.customer = Validator.requireNotNull(customer, "Customer");
        this.pricing = Validator.requireNotNull(pricing, "Pricing service");
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
        checkCanAddItem();
        OrderItem item = OrderItem.of(product, quantity); // validates the product and quantity
        product.reduceStock(quantity);                    // validates the stock
        items.add(item);                                  // only reached if both checks passed
    }

    public OrderResult calculatePricing() {
        if (items.isEmpty()) {
            throw new InvalidStateException("Order has no items.");
        }
        double[] prices = new double[items.size()];
        int[] quantities = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            prices[i] = items.get(i).unitPrice();
            quantities[i] = items.get(i).quantity();
        }
        return pricing.calculate(prices, quantities,
                customer.getAddress().zone(), customer.isLoyaltyMember());
    }

    public void pay(PaymentMethod method) {
        checkCanPay();
        payment = new Payment(calculatePricing().total, method);
    }

    public void confirmPayment() {
        requirePendingPayment();
        payment.markSuccessful();
        moveTo(OrderStatus.PAID);
    }

    public void failPayment() {
        requirePendingPayment();
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
        returnStock();
        settlePaymentAfterCancel();
    }

    // ---------- small private helpers ----------

    private void checkCanAddItem() {
        if (status != OrderStatus.NEW) {
            throw new InvalidStateException("Items can only be added to a new order.");
        }
        if (hasPendingPayment()) {
            throw new InvalidStateException("A payment is in progress. Items cannot be added now.");
        }
        if (items.size() >= MAX_ITEMS) {
            throw new InvalidStateException("An order cannot have more than " + MAX_ITEMS + " items.");
        }
    }

    private void checkCanPay() {
        if (status != OrderStatus.NEW) {
            throw new InvalidStateException("Only a new order can be paid.");
        }
        if (items.isEmpty()) {
            throw new InvalidStateException("Cannot pay for an empty order.");
        }
        if (hasPendingPayment()) {
            throw new InvalidStateException("A payment is already in progress.");
        }
    }

    private boolean hasPendingPayment() {
        return payment != null && payment.getStatus() == PaymentStatus.PENDING;
    }

    private void requirePendingPayment() {
        if (!hasPendingPayment()) {
            throw new InvalidStateException("There is no pending payment.");
        }
    }

    private void returnStock() {
        for (OrderItem item : items) {
            item.product().addStock(item.quantity());
        }
    }

    private void settlePaymentAfterCancel() {
        if (payment == null) {
            return;
        }
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.markFailed();
        } else if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            payment.refund();
        }
    }

    // The only place the status changes, so every change goes through the rules.
    private void moveTo(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new InvalidStateException("Cannot change order from " + status + " to " + next + ".");
        }
        status = next;
    }
}