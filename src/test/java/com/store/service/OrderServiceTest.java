package com.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.store.exception.NotFoundException;
import com.store.exception.ValidationException;
import com.store.model.Address;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.OrderStatus;
import com.store.model.PaymentMethod;
import com.store.model.Product;
import com.store.repository.InMemoryOrderRepository;
import com.store.repository.InMemoryProductRepository;
import com.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderServiceTest {

    private ProductRepository products;
    private OrderService service;
    private Customer customer;

    @BeforeEach
    void setUp() {
        products = new InMemoryProductRepository();
        products.save(new Product("P1", "Notebook", 50.00, 10));
        service = new OrderService(new InMemoryOrderRepository(), products);
        Address address = new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
        customer = new Customer("C1", "Ada Obi", "ada@example.com", address);
    }

    @Test
    void createdOrderCanBeFoundAgain() {
        Order created = service.createOrder("O1", customer);
        assertSame(created, service.getOrder("O1"));
    }

    @Test
    void duplicateOrderIdIsRejected() {
        service.createOrder("O1", customer);
        assertThrows(ValidationException.class, () -> service.createOrder("O1", customer));
    }

    @Test
    void addItemTakesStockFromTheStoredProduct() {
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 2);
        assertEquals(8, products.findById("P1").get().getStock());
        assertEquals(1, service.getOrder("O1").getItems().size());
    }

    @Test
    void unknownProductIsNotFound() {
        service.createOrder("O1", customer);
        NotFoundException e = assertThrows(NotFoundException.class, () -> service.addItem("O1", "P9", 1));
        assertEquals("Product P9 was not found.", e.getMessage());
    }

    @Test
    void unknownOrderIsNotFound() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> service.getOrder("O9"));
        assertEquals("Order O9 was not found.", e.getMessage());
    }

    @Test
    void payAndConfirmMarksTheOrderPaid() {
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        service.payAndConfirm("O1", PaymentMethod.CARD);
        assertEquals(OrderStatus.PAID, service.getOrder("O1").getStatus());
    }
}