package com.store.app;

import com.store.analytics.LoadResult;
import com.store.exception.DataFileException;
import com.store.exception.DuplicateOrderException;
import com.store.exception.InvalidStateException;
import com.store.exception.OrderNotFoundException;
import com.store.exception.PaymentFailedException;
import com.store.logging.LogSetup;
import com.store.model.Address;
import com.store.model.Customer;
import com.store.model.PaymentMethod;
import com.store.model.Product;
import com.store.payment.SimulatedPaymentGateway;
import com.store.repository.InMemoryOrderRepository;
import com.store.repository.InMemoryProductRepository;
import com.store.repository.ProductRepository;
import com.store.service.OrderService;
import com.store.storage.ProductFileStore;

import java.nio.file.Path;
import java.util.logging.Level;

// Shows how the program behaves when things go wrong. Check logs/store.log afterwards.
public class ReliabilityDemo {

    public static void main(String[] args) {
        LogSetup.init(Level.FINE, "logs/store.log");

        ProductRepository products = new InMemoryProductRepository();
        products.save(new Product("P1", "Notebook", 50.00, 100));
        products.save(new Product("P3", "Backpack", 120.00, 20));

        // The simulated payment provider declines anything above 500.00.
        OrderService service = new OrderService(new InMemoryOrderRepository(), products,
                new SimulatedPaymentGateway(500.00));
        Customer ada = new Customer("C1", "Ada Obi", "ada@example.com",
                new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local"));

        saveAndReloadProducts(products);
        loadAMissingFile();
        placeANormalOrder(service, ada);
        createADuplicateOrder(service, ada);
        findAnUnknownOrder(service);
        payTooMuch(service, ada);
        payTwice(service);
    }

    private static void saveAndReloadProducts(ProductRepository products) {
        Path file = Path.of("demo-products.csv");
        try {
            ProductFileStore.save(products.findAll(), file);
            LoadResult<Product> result = ProductFileStore.load(file);
            System.out.println("Reloaded " + result.items().size() + " products from the file.");
        } catch (DataFileException e) {
            System.out.println("File problem: " + e.getMessage());
        }
    }

    private static void loadAMissingFile() {
        try {
            ProductFileStore.load(Path.of("does-not-exist.csv"));
        } catch (DataFileException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }

    private static void placeANormalOrder(OrderService service, Customer customer) {
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 2);
        service.checkout("O1", PaymentMethod.CARD);
        System.out.println("Order O1 is " + service.getOrder("O1").getStatus());
    }

    private static void createADuplicateOrder(OrderService service, Customer customer) {
        try {
            service.createOrder("O1", customer);
        } catch (DuplicateOrderException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }

    private static void findAnUnknownOrder(OrderService service) {
        try {
            service.getOrder("O99");
        } catch (OrderNotFoundException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }

    private static void payTooMuch(OrderService service, Customer customer) {
        service.createOrder("O2", customer);
        service.addItem("O2", "P3", 5); // total is about 580, above the 500 limit
        try {
            service.checkout("O2", PaymentMethod.CARD);
        } catch (PaymentFailedException e) {
            System.out.println("Expected failure: " + e.getMessage());
            System.out.println("Order O2 is still " + service.getOrder("O2").getStatus() + ", so it can be paid again.");
        }
    }

    private static void payTwice(OrderService service) {
        try {
            service.checkout("O1", PaymentMethod.CARD);
        } catch (InvalidStateException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }
}