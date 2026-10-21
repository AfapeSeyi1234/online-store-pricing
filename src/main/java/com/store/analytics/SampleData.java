package com.store.analytics;

import com.store.model.Address;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.PaymentMethod;
import com.store.model.Product;
import com.store.pricing.PricingService;

import java.time.LocalDate;
import java.util.List;

public class SampleData {

    private final List<Product> products;
    private final List<Customer> customers;
    private final List<Order> orders;

    private SampleData(List<Product> products, List<Customer> customers, List<Order> orders) {
        this.products = products;
        this.customers = customers;
        this.orders = orders;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Order> getOrders() {
        return orders;
    }

    // Builds a fresh set of data every time, so tests never share changed objects.
    public static SampleData create() {
        Product notebook = new Product("P1", "Notebook", 50.00, 100);
        Product pen = new Product("P2", "Pen", 2.50, 500);
        Product backpack = new Product("P3", "Backpack", 120.00, 20);
        Product lamp = new Product("P4", "Desk Lamp", 35.00, 40);
        Product cable = new Product("P5", "USB Cable", 8.99, 200);
        Product headphones = new Product("P6", "Headphones", 75.00, 30);

        Customer ada = new Customer("C1", "Ada Obi", "ada@example.com",
                new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local"));
        ada.makeLoyaltyMember();
        Customer tunde = new Customer("C2", "Tunde Bello", "tunde@example.com",
                new Address("5 Ring Road", "Ibadan", "Nigeria", "regional"));
        Customer chioma = new Customer("C3", "Chioma Eze", "chioma@example.com",
                new Address("8 Admiralty Way", "Lagos", "Nigeria", "local"));
        Customer john = new Customer("C4", "John Smith", "john@example.com",
                new Address("20 High Street", "London", "United Kingdom", "international"));

        Order o1 = newOrder("O1", ada, 1);
        o1.addItem(notebook, 2);
        o1.addItem(pen, 10);
        payShipAndDeliver(o1);

        Order o2 = newOrder("O2", tunde, 3);
        o2.addItem(backpack, 1);
        payAndShip(o2);

        Order o3 = newOrder("O3", chioma, 5);
        o3.addItem(headphones, 1);
        o3.addItem(cable, 2);
        pay(o3);

        Order o4 = newOrder("O4", ada, 8);
        o4.addItem(lamp, 3); // stays NEW

        Order o5 = newOrder("O5", john, 10);
        o5.addItem(notebook, 1);
        o5.cancel();

        Order o6 = newOrder("O6", tunde, 12);
        o6.addItem(pen, 20);
        o6.addItem(notebook, 3);
        payShipAndDeliver(o6);

        Order o7 = newOrder("O7", chioma, 15);
        o7.addItem(backpack, 2);
        pay(o7);

        Order o8 = newOrder("O8", ada, 18);
        o8.addItem(cable, 5);
        o8.addItem(headphones, 1);
        payAndShip(o8);

        return new SampleData(
                List.of(notebook, pen, backpack, lamp, cable, headphones),
                List.of(ada, tunde, chioma, john),
                List.of(o1, o2, o3, o4, o5, o6, o7, o8));
    }

    // Product records as text, with deliberate mistakes to show how bad records are handled.
    public static List<String> productCsvLines() {
        return List.of(
                "id,name,price,stock",
                "P1,Notebook,50.00,100",
                "P2,Pen,2.50,500",
                "P2,Pen Duplicate,3.00,10",
                "P3,Backpack,abc,20",
                "P4,Desk Lamp,35.00,-5",
                "P5,USB Cable,8.99",
                "",
                "P6,Headphones,75.00,30");
    }

    private static Order newOrder(String id, Customer customer, int dayOfOctober) {
        return new Order(id, customer, PricingService.standard(), LocalDate.of(2026, 10, dayOfOctober));
    }

    private static void pay(Order order) {
        order.pay(PaymentMethod.CARD);
        order.confirmPayment();
    }

    private static void payAndShip(Order order) {
        pay(order);
        order.ship();
    }

    private static void payShipAndDeliver(Order order) {
        payAndShip(order);
        order.confirmDelivery();
    }
}