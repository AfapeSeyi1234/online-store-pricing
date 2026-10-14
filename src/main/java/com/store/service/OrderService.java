package com.store.service;

import com.store.exception.NotFoundException;
import com.store.exception.ValidationException;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.PaymentMethod;
import com.store.model.Product;
import com.store.repository.OrderRepository;
import com.store.repository.ProductRepository;

// Coordinates the steps of placing and paying for an order.
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    public Order createOrder(String orderId, Customer customer) {
        if (orders.findById(orderId).isPresent()) {
            throw new ValidationException("Order " + orderId + " already exists.");
        }
        Order order = new Order(orderId, customer);
        orders.save(order);
        return order;
    }

    public Order getOrder(String orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " was not found."));
    }

    public void addItem(String orderId, String productId, int quantity) {
        Order order = getOrder(orderId);
        Product product = products.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product " + productId + " was not found."));
        order.addItem(product, quantity);
    }

    // A real shop would wait for the payment provider. Here we confirm straight away.
    public void payAndConfirm(String orderId, PaymentMethod method) {
        Order order = getOrder(orderId);
        order.pay(method);
        order.confirmPayment();
    }
}