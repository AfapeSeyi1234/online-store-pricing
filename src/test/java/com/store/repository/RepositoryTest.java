package com.store.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.model.Address;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.Product;
import org.junit.jupiter.api.Test;

public class RepositoryTest {

    @Test
    void savedProductCanBeFoundById() {
        ProductRepository repo = new InMemoryProductRepository();
        Product notebook = new Product("P1", "Notebook", 50.00, 10);
        repo.save(notebook);
        assertSame(notebook, repo.findById("P1").get());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void missingProductGivesAnEmptyResult() {
        ProductRepository repo = new InMemoryProductRepository();
        assertTrue(repo.findById("nope").isEmpty());
    }

    @Test
    void savedOrderCanBeFoundById() {
        OrderRepository repo = new InMemoryOrderRepository();
        Address address = new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
        Customer customer = new Customer("C1", "Ada Obi", "ada@example.com", address);
        Order order = new Order("O1", customer);
        repo.save(order);
        assertSame(order, repo.findById("O1").get());
    }
}