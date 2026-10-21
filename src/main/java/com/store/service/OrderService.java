package com.store.service;

import com.store.exception.DuplicateOrderException;
import com.store.exception.InvalidStateException;
import com.store.exception.OrderNotFoundException;
import com.store.exception.PaymentFailedException;
import com.store.exception.ProductNotFoundException;
import com.store.exception.ValidationException;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.PaymentMethod;
import com.store.model.Product;
import com.store.payment.PaymentGateway;
import com.store.payment.SimulatedPaymentGateway;
import com.store.repository.OrderRepository;
import com.store.repository.ProductRepository;
import com.store.validation.Validator;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

// Coordinates the steps of placing and paying for an order.
// Logging rule: log order ids, customer ids and amounts only.
// Never log emails, names, addresses, payment method or card details.
public class OrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    private final OrderRepository orders;
    private final ProductRepository products;
    private final PaymentGateway gateway;

    public OrderService(OrderRepository orders, ProductRepository products) {
        this(orders, products, SimulatedPaymentGateway.alwaysApprove());
    }

    public OrderService(OrderRepository orders, ProductRepository products, PaymentGateway gateway) {
        this.orders = orders;
        this.products = products;
        this.gateway = Validator.requireNotNull(gateway, "Payment gateway");
    }

    public Order createOrder(String orderId, Customer customer) {
        LOGGER.fine("Creating order " + orderId);
        if (orders.findById(orderId).isPresent()) {
            LOGGER.warning("Duplicate order id rejected: " + orderId);
            throw new DuplicateOrderException(orderId);
        }
        Order order = new Order(orderId, customer);
        orders.save(order);
        LOGGER.info("Order " + orderId + " created for customer " + customer.getId());
        return order;
    }

    public Order getOrder(String orderId) {
        return orders.findById(orderId).orElseThrow(() -> {
            LOGGER.warning("Order not found: " + orderId);
            return new OrderNotFoundException(orderId);
        });
    }

    public void addItem(String orderId, String productId, int quantity) {
        Order order = getOrder(orderId);
        Product product = products.findById(productId).orElseThrow(() -> {
            LOGGER.warning("Product not found: " + productId);
            return new ProductNotFoundException(productId);
        });
        try {
            order.addItem(product, quantity);
            LOGGER.fine("Added " + quantity + " x " + productId + " to order " + orderId);
        } catch (ValidationException | InvalidStateException e) {
            LOGGER.warning("Item not added to order " + orderId + ": " + e.getMessage());
            throw e;
        }
    }

    // Pays for the order. If the payment is declined, the order stays NEW so the customer can try again.
    public void checkout(String orderId, PaymentMethod method) {
        Order order = getOrder(orderId);
        try {
            order.pay(method);
            double amount = order.getPayment().getAmount();
            gateway.charge(orderId, amount);
            order.confirmPayment();
            LOGGER.info("Order " + orderId + " paid, amount " + String.format(Locale.US, "%.2f", amount));
        } catch (PaymentFailedException e) {
            order.failPayment();
            LOGGER.warning("Payment failed for order " + orderId + ": " + e.getMessage());
            throw e;
        } catch (InvalidStateException e) {
            LOGGER.warning("Checkout not allowed for order " + orderId + ": " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during checkout of order " + orderId, e);
            throw e;
        } finally {
            LOGGER.fine("Checkout finished for order " + orderId); // runs on success and on failure
        }
    }

    // Kept from Week 3 so existing code still works.
    public void payAndConfirm(String orderId, PaymentMethod method) {
        checkout(orderId, method);
    }
}