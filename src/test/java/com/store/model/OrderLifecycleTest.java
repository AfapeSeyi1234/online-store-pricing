package com.store.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.pricing.OrderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderLifecycleTest {

    private Product notebook;
    private Customer customer;
    private Order order;

    // Runs before every test, so each test starts fresh.
    @BeforeEach
    void setUp() {
        notebook = new Product("P1", "Notebook", 50.00, 10);
        Address address = new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
        customer = new Customer("C1", "Ada Obi", "ada@example.com", address);
        order = new Order("O1", customer);
    }

    private void payForOrder() {
        order.addItem(notebook, 1);
        order.pay(PaymentMethod.CARD);
        order.confirmPayment();
    }

    @Test
    void newOrderStartsAsNewAndEmpty() {
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void addingAnItemReducesStock() {
        order.addItem(notebook, 3);
        assertEquals(7, notebook.getStock());
        assertEquals(1, order.getItems().size());
    }

    @Test
    void cannotAddMoreThanTheStock() {
        assertThrows(IllegalArgumentException.class, () -> order.addItem(notebook, 11));
        assertEquals(10, notebook.getStock());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void pricingUsesWeekOneRules() {
        order.addItem(notebook, 1);
        OrderResult result = order.calculatePricing();
        assertEquals(56.06, result.total, 0.01);
    }

    @Test
    void cannotPayForAnEmptyOrder() {
        assertThrows(IllegalStateException.class, () -> order.pay(PaymentMethod.CARD));
    }

    @Test
    void successfulPaymentMovesOrderToPaid() {
        payForOrder();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.SUCCESSFUL, order.getPayment().getStatus());
        assertEquals(56.06, order.getPayment().getAmount(), 0.01);
    }

    @Test
    void failedPaymentKeepsOrderNewAndAllowsRetry() {
        order.addItem(notebook, 1);
        order.pay(PaymentMethod.CARD);
        order.failPayment();
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(PaymentStatus.FAILED, order.getPayment().getStatus());

        order.pay(PaymentMethod.BANK_TRANSFER);
        assertEquals(PaymentStatus.PENDING, order.getPayment().getStatus());
    }

    @Test
    void paymentCannotBeConfirmedTwice() {
        payForOrder();
        assertThrows(IllegalStateException.class, () -> order.confirmPayment());
    }

    @Test
    void cannotShipBeforePayment() {
        order.addItem(notebook, 1);
        assertThrows(IllegalStateException.class, () -> order.ship());
    }

    @Test
    void cannotAddItemsAfterPayment() {
        payForOrder();
        assertThrows(IllegalStateException.class, () -> order.addItem(notebook, 1));
    }

    @Test
    void fullLifecycleEndsDelivered() {
        payForOrder();
        order.ship();
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(DeliveryStatus.IN_TRANSIT, order.getDelivery().getStatus());

        order.confirmDelivery();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(DeliveryStatus.DELIVERED, order.getDelivery().getStatus());
    }

    @Test
    void cancellingANewOrderGivesStockBack() {
        order.addItem(notebook, 4);
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(10, notebook.getStock());
    }

    @Test
    void cancellingAPaidOrderRefundsThePayment() {
        payForOrder();
        order.cancel();
        assertEquals(PaymentStatus.REFUNDED, order.getPayment().getStatus());
        assertEquals(10, notebook.getStock());
    }

    @Test
    void cannotCancelAfterShipping() {
        payForOrder();
        order.ship();
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    @Test
    void itemListCannotBeChangedFromOutside() {
        order.addItem(notebook, 1);
        assertThrows(UnsupportedOperationException.class, () -> order.getItems().clear());
    }
}