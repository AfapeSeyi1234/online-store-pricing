package com.store.model;

import com.store.exception.InvalidOrderStateException;
import com.store.pricing.OrderResult;
import com.store.pricing.PricingService;
import com.store.validation.Validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    public static final int MAX_ITEMS = 20;

    private final String id;
    private final Customer customer;
    private final PricingService pricing;
    private final LocalDate createdOn;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.NEW;
    private Payment payment;     // empty until pay() is called
    private Delivery delivery;   // empty until ship() is called

    // Convenient default: uses the standard store pricing rules.
    public Order(String id, Customer customer) {
        this(id, customer, PricingService.standard());
    }

    // Used when a different pricing setup is needed (for example in tests or promotions).
    // Keeping both means simple code stays simple, while Order is not locked to one pricing setup.
    public Order(String id, Customer customer, PricingService pricing) {
        this(id, customer, pricing, LocalDate.now());
    }

    // Used when the order date must be set, for example for sample data and reports.
    public Order(String id, Customer customer, PricingService pricing, LocalDate createdOn) {
        this.id = Validator.requireText(id, "Order id");
        this.customer = Validator.requireNotNull(customer, "Customer");
        this.pricing = Validator.requireNotNull(pricing, "Pricing service");
        this.createdOn = Validator.requireNotNull(createdOn, "Order date");
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

    public LocalDate getCreatedOn() {
        return createdOn;
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

    // The final amount to pay, or 0 if the order has no items yet.
    public double getTotal() {
        if (items.isEmpty()) {
            return 0;
        }
        return calculatePricing().total;
    }

    public void addItem(Product product, int quantity) {
        checkCanAddItem();
        OrderItem item = OrderItem.of(product, quantity); // validates the product and quantity
        product.reduceStock(quantity);                    // validates the stock
        items.add(item);                                  // only reached if both checks passed
    }

    public OrderResult calculatePricing() {
        if (items.isEmpty()) {
            throw new InvalidOrderStateException("Order has no items.");
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
            throw new InvalidOrderStateException("Items can only be added to a new order.");
        }
        if (hasPendingPayment()) {
            throw new InvalidOrderStateException("A payment is in progress. Items cannot be added now.");
        }
        if (items.size() >= MAX_ITEMS) {
            throw new InvalidOrderStateException("An order cannot have more than " + MAX_ITEMS + " items.");
        }
    }

    private void checkCanPay() {
        if (status != OrderStatus.NEW) {
            throw new InvalidOrderStateException("Only a new order can be paid.");
        }
        if (items.isEmpty()) {
            throw new InvalidOrderStateException("Cannot pay for an empty order.");
        }
        if (hasPendingPayment()) {
            throw new InvalidOrderStateException("A payment is already in progress.");
        }
    }

    private boolean hasPendingPayment() {
        return payment != null && payment.getStatus() == PaymentStatus.PENDING;
    }

    private void requirePendingPayment() {
        if (!hasPendingPayment()) {
            throw new InvalidOrderStateException("There is no pending payment.");
        }
    }

    private void returnStock() {
        for (OrderItem item : items) {
            item.product().addStock(1);
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
            throw new InvalidOrderStateException("Cannot change order from " + status + " to " + next + ".");
        }
        status = next;
    }
}