package com.store.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OrderCancelRegressionTest {

    @Test
    void cancellingAnOrderWithSeveralLinesReturnsAllUnitsToStock() {
        Product notebook = new Product("P1", "Notebook", 50.00, 10);
        Product pen = new Product("P2", "Pen", 2.50, 100);
        Address address = new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
        Customer customer = new Customer("C1", "Ada Obi", "ada@example.com", address);
        Order order = new Order("O1", customer);
        order.addItem(notebook, 4);
        order.addItem(pen, 25);

        order.cancel();

        assertEquals(10, notebook.getStock());
        assertEquals(100, pen.getStock());
    }
}