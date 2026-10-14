package com.store.app;

import com.store.model.Address;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.PaymentMethod;
import com.store.model.Product;
import com.store.pricing.OrderResult;
import com.store.repository.InMemoryOrderRepository;
import com.store.repository.InMemoryProductRepository;
import com.store.repository.ProductRepository;
import com.store.service.OrderService;

public class DomainDemo {

    public static void main(String[] args) {
        ProductRepository products = new InMemoryProductRepository();
        OrderService service = new OrderService(new InMemoryOrderRepository(), products);

        products.save(new Product("P1", "Notebook", 50.00, 10));
        Address address = new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
        Customer customer = new Customer("C1", "Ada Obi", "ada@example.com", address);

        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 2);

        Order order = service.getOrder("O1");
        OrderResult price = order.calculatePricing();
        System.out.printf("Total to pay: %.2f%n", price.total);
        System.out.println("Stock left: " + products.findById("P1").get().getStock());

        service.payAndConfirm("O1", PaymentMethod.CARD);
        System.out.println("Order status: " + order.getStatus());

        order.ship();
        System.out.println("Delivery status: " + order.getDelivery().getStatus());

        order.confirmDelivery();
        System.out.println("Order status: " + order.getStatus());
    }
}