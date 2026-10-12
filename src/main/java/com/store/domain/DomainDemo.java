package com.store.domain;

import com.store.OrderResult;

public class DomainDemo {

    public static void main(String[] args) {
        Product notebook = new Product("P1", "Notebook", 50.00, 10);
        Address address = new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
        Customer customer = new Customer("C1", "Ada Obi", "ada@example.com", address);

        Order order = new Order("O1", customer);
        order.addItem(notebook, 2);

        OrderResult price = order.calculatePricing();
        System.out.printf("Total to pay: %.2f%n", price.total);
        System.out.println("Stock left: " + notebook.getStock());

        order.pay(PaymentMethod.CARD);
        order.confirmPayment();
        System.out.println("Order status: " + order.getStatus());

        order.ship();
        System.out.println("Delivery status: " + order.getDelivery().getStatus());

        order.confirmDelivery();
        System.out.println("Order status: " + order.getStatus());
    }
}