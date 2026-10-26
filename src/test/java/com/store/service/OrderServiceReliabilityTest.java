package com.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.exception.DuplicateOrderException;
import com.store.exception.InvalidOrderStateException;
import com.store.exception.OrderNotFoundException;
import com.store.exception.PaymentFailedException;
import com.store.exception.ProductNotFoundException;
import com.store.exception.ValidationException;
import com.store.model.Address;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.OrderStatus;
import com.store.model.PaymentMethod;
import com.store.model.PaymentStatus;
import com.store.model.Product;
import com.store.payment.PaymentGateway;
import com.store.payment.SimulatedPaymentGateway;
import com.store.repository.InMemoryOrderRepository;
import com.store.repository.InMemoryProductRepository;
import com.store.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderServiceReliabilityTest {

    private static final Logger SERVICE_LOGGER = Logger.getLogger(OrderService.class.getName());

    private ProductRepository products;
    private Product notebook;
    private Customer customer;
    private CapturingHandler logs;
    private Level oldLevel;
    private boolean oldUseParentHandlers;

    @BeforeEach
    void setUp() {
        notebook = new Product("P1", "Notebook", 50.00, 10);
        products = new InMemoryProductRepository();
        products.save(notebook);
        Address address = new Address("12 Allen Avenue", "Ikeja", "Nigeria", "local");
        customer = new Customer("C1", "Ada Obi", "ada@example.com", address);

        // Capture the log messages so the tests can check them.
        logs = new CapturingHandler();
        oldLevel = SERVICE_LOGGER.getLevel();
        oldUseParentHandlers = SERVICE_LOGGER.getUseParentHandlers();
        SERVICE_LOGGER.setLevel(Level.ALL);
        SERVICE_LOGGER.setUseParentHandlers(false); // keeps the test output quiet
        SERVICE_LOGGER.addHandler(logs);
    }

    @AfterEach
    void tearDown() {
        SERVICE_LOGGER.removeHandler(logs);
        SERVICE_LOGGER.setUseParentHandlers(oldUseParentHandlers);
        SERVICE_LOGGER.setLevel(oldLevel);
    }

    private OrderService serviceWith(PaymentGateway gateway) {
        return new OrderService(new InMemoryOrderRepository(), products, gateway);
    }

    private OrderService approvedService() {
        return serviceWith(SimulatedPaymentGateway.alwaysApprove());
    }

    // ---------- missing records and duplicates ----------

    @Test
    void creatingAnOrderWithADuplicateIdThrowsDuplicateOrderException() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        DuplicateOrderException e = assertThrows(DuplicateOrderException.class,
                () -> service.createOrder("O1", customer));
        assertEquals("Order O1 already exists.", e.getMessage());
    }

    @Test
    void gettingAnUnknownOrderThrowsOrderNotFoundException() {
        OrderService service = approvedService();
        OrderNotFoundException e = assertThrows(OrderNotFoundException.class,
                () -> service.getOrder("O9"));
        assertEquals("O9", e.getOrderId());
    }

    @Test
    void addingAnUnknownProductThrowsProductNotFoundException() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        assertThrows(ProductNotFoundException.class, () -> service.addItem("O1", "P9", 1));
    }

    @Test
    void addingMoreThanTheStockFailsAndLeavesStockUnchanged() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        assertThrows(ValidationException.class, () -> service.addItem("O1", "P1", 11));
        assertEquals(10, notebook.getStock());
        assertTrue(service.getOrder("O1").getItems().isEmpty());
    }

    // ---------- payments ----------

    @Test
    void approvedPaymentMarksTheOrderPaid() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        service.checkout("O1", PaymentMethod.CARD);
        Order order = service.getOrder("O1");
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.SUCCESSFUL, order.getPayment().getStatus());
    }

    @Test
    void declinedPaymentThrowsPaymentFailedException() {
        OrderService service = serviceWith(new SimulatedPaymentGateway(50.00));
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1); // total is 56.06
        assertThrows(PaymentFailedException.class, () -> service.checkout("O1", PaymentMethod.CARD));
    }

    @Test
    void declinedPaymentKeepsTheOrderNewAndMarksThePaymentFailed() {
        OrderService service = serviceWith(new SimulatedPaymentGateway(50.00));
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        assertThrows(PaymentFailedException.class, () -> service.checkout("O1", PaymentMethod.CARD));
        Order order = service.getOrder("O1");
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(PaymentStatus.FAILED, order.getPayment().getStatus());
    }

    @Test
    void paymentExactlyAtTheLimitIsApproved() {
        OrderService service = serviceWith(new SimulatedPaymentGateway(56.06));
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        service.checkout("O1", PaymentMethod.CARD);
        assertEquals(OrderStatus.PAID, service.getOrder("O1").getStatus());
    }

    @Test
    void paymentOneCentAboveTheLimitIsDeclined() {
        OrderService service = serviceWith(new SimulatedPaymentGateway(56.05));
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        assertThrows(PaymentFailedException.class, () -> service.checkout("O1", PaymentMethod.CARD));
    }

    // ---------- invalid state changes ----------

    @Test
    void checkingOutAPaidOrderAgainThrowsInvalidOrderStateException() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        service.checkout("O1", PaymentMethod.CARD);
        assertThrows(InvalidOrderStateException.class,
                () -> service.checkout("O1", PaymentMethod.CARD));
    }

    @Test
    void checkingOutAnEmptyOrderThrowsInvalidOrderStateException() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        assertThrows(InvalidOrderStateException.class,
                () -> service.checkout("O1", PaymentMethod.CARD));
    }

    @Test
    void cancellingAnOrderTwiceThrowsInvalidOrderStateException() {
        OrderService service = approvedService();
        Order order = service.createOrder("O1", customer);
        order.cancel();
        assertThrows(InvalidOrderStateException.class, order::cancel);
    }

    // ---------- logging ----------

    @Test
    void aNormalOrderIsLoggedAtInfoAndDebugLevel() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        assertTrue(logs.hasMessage(Level.INFO, "Order O1 created"));
        assertTrue(logs.hasMessage(Level.FINE, "Added 1 x P1 to order O1"));
    }

    @Test
    void aDuplicateOrderIsLoggedAsAWarning() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        assertThrows(DuplicateOrderException.class, () -> service.createOrder("O1", customer));
        assertTrue(logs.hasMessage(Level.WARNING, "Duplicate order id rejected: O1"));
    }

    @Test
    void aDeclinedPaymentIsLoggedAsAWarning() {
        OrderService service = serviceWith(new SimulatedPaymentGateway(50.00));
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        assertThrows(PaymentFailedException.class, () -> service.checkout("O1", PaymentMethod.CARD));
        assertTrue(logs.hasMessage(Level.WARNING, "Payment failed for order O1"));
    }

    @Test
    void checkoutLogsDoNotContainPersonalData() {
        OrderService service = approvedService();
        service.createOrder("O1", customer);
        service.addItem("O1", "P1", 1);
        service.checkout("O1", PaymentMethod.CARD);

        assertFalse(logs.messages.isEmpty());
        for (String message : logs.messages) {
            assertFalse(message.contains("ada@example.com"), message);
            assertFalse(message.contains("Ada Obi"), message);
            assertFalse(message.contains("Allen Avenue"), message);
            assertFalse(message.contains("CARD"), message);
        }
    }

    // Collects log messages so tests can look at them.
    private static class CapturingHandler extends Handler {

        private final List<String> messages = new ArrayList<>();
        private final List<Level> levels = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
            levels.add(record.getLevel());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        boolean hasMessage(Level level, String text) {
            for (int i = 0; i < messages.size(); i++) {
                if (levels.get(i).equals(level) && messages.get(i).contains(text)) {
                    return true;
                }
            }
            return false;
        }
    }
}