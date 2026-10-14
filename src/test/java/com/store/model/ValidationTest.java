package com.store.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ValidationTest {

    private Address validAddress() {
        return new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
    }

    // ---------- Address ----------

    @Test
    void addressRejectsBlankStreet() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Address(" ", "Ikeja", "Nigeria", "local"));
        assertEquals("Street is required.", e.getMessage());
    }

    @Test
    void addressRejectsUnknownZone() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Address("1 Road", "Ikeja", "Nigeria", "mars"));
        assertEquals("Zone must be local, regional or international.", e.getMessage());
    }

    @Test
    void addressChangesZoneToLowercase() {
        Address a = new Address("1 Road", "Ikeja", "Nigeria", "LOCAL");
        assertEquals("local", a.zone());
    }

    // ---------- Product ----------

    @Test
    void productRejectsZeroPrice() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Product("P1", "Notebook", 0, 5));
        assertEquals("Price must be greater than 0 and at most 100000.", e.getMessage());
    }

    @Test
    void productRejectsNegativeStock() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Product("P1", "Notebook", 10, -1));
        assertEquals("Stock cannot be negative.", e.getMessage());
    }

    @Test
    void reducingStockLowersTheStock() {
        Product p = new Product("P1", "Notebook", 50, 10);
        p.reduceStock(3);
        assertEquals(7, p.getStock());
    }

    @Test
    void cannotReduceStockBelowZero() {
        Product p = new Product("P1", "Notebook", 50, 10);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> p.reduceStock(11));
        assertEquals("Not enough stock for Notebook. Available: 10.", e.getMessage());
        assertEquals(10, p.getStock());
    }

    // ---------- Customer ----------

    @Test
    void customerRejectsInvalidEmail() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Customer("C1", "Ada Obi", "not-an-email", validAddress()));
        assertEquals("Email is not valid.", e.getMessage());
    }

    @Test
    void customerNeedsAnAddress() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Customer("C1", "Ada Obi", "ada@example.com", null));
        assertEquals("Address is required.", e.getMessage());
    }

    @Test
    void newCustomerIsNotLoyaltyMemberUntilUpgraded() {
        Customer c = new Customer("C1", "Ada Obi", "ada@example.com", validAddress());
        assertFalse(c.isLoyaltyMember());
        c.makeLoyaltyMember();
        assertTrue(c.isLoyaltyMember());
    }

    // ---------- OrderItem ----------

    @Test
    void orderItemRejectsQuantityZero() {
        Product p = new Product("P1", "Notebook", 50, 10);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OrderItem.of(p, 0));
        assertEquals("Quantity must be between 1 and 100.", e.getMessage());
    }

    @Test
    void orderItemCalculatesLineTotal() {
        Product p = new Product("P1", "Notebook", 50, 10);
        assertEquals(150.00, OrderItem.of(p, 3).lineTotal(), 0.01);
    }

    @Test
    void orderItemKeepsThePriceFromWhenItWasCreated() {
        Product p = new Product("P1", "Notebook", 50, 10);
        OrderItem item = OrderItem.of(p, 1);
        p.changePrice(80);
        assertEquals(50.00, item.unitPrice(), 0.01);
    }

    // ---------- Payment and status rules ----------

    @Test
    void paymentRejectsZeroAmount() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Payment(0, PaymentMethod.CARD));
        assertEquals("Payment amount must be greater than 0.", e.getMessage());
    }

    @Test
    void orderStatusRulesAllowOnlyValidMoves() {
        assertTrue(OrderStatus.NEW.canMoveTo(OrderStatus.PAID));
        assertFalse(OrderStatus.NEW.canMoveTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.DELIVERED.canMoveTo(OrderStatus.CANCELLED));
    }
}