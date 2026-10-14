package com.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.store.pricing.OrderResult;
import org.junit.jupiter.api.Test;

public class OrderCalculatorTest {

    // helper: an order with one item
    private OrderResult oneItem(double price, int quantity, String location, boolean member) {
        return OrderCalculator.calculate(new double[]{price}, new int[]{quantity}, location, member);
    }

    // ---------- NORMAL CASES ----------

    @Test
    void smallLocalOrderHasNoDiscount() {
        OrderResult r = oneItem(10.00, 2, "local", false);
        assertEquals(20.00, r.subtotal, 0.01);
        assertEquals(0.00, r.discount, 0.01);
        assertEquals(5.00, r.deliveryFee, 0.01);
        assertEquals(1.50, r.tax, 0.01);
        assertEquals(26.50, r.total, 0.01);
    }

    @Test
    void fiftyGetsFivePercentDiscount() {
        OrderResult r = oneItem(50.00, 1, "local", false);
        assertEquals(2.50, r.discount, 0.01);
        assertEquals(3.56, r.tax, 0.01);
        assertEquals(56.06, r.total, 0.01);
    }

    @Test
    void hundredGetsTenPercentDiscountAndFreeLocalDelivery() {
        OrderResult r = oneItem(100.00, 1, "local", false);
        assertEquals(10.00, r.discount, 0.01);
        assertEquals(0.00, r.deliveryFee, 0.01);
        assertEquals(96.75, r.total, 0.01);
    }

    @Test
    void tenItemsGetBulkDiscount() {
        OrderResult r = oneItem(1.00, 10, "local", false);
        assertEquals(0.30, r.discount, 0.01);
        assertEquals(15.43, r.total, 0.01);
    }

    @Test
    void memberGetsExtraFivePercent() {
        OrderResult r = oneItem(30.00, 1, "local", true);
        assertEquals(1.50, r.discount, 0.01);
        assertEquals(35.64, r.total, 0.01);
    }

    @Test
    void severalItemsAreAddedUp() {
        double[] prices = {15.00, 25.00};
        int[] quantities = {2, 1};
        OrderResult r = OrderCalculator.calculate(prices, quantities, "local", false);
        assertEquals(55.00, r.subtotal, 0.01);
        assertEquals(2.75, r.discount, 0.01);
        assertEquals(61.17, r.total, 0.01);
    }

    @Test
    void discountIsCappedAtFifteenPercent() {
        // 10% + 3% + 5% = 18%, but the cap is 15%
        OrderResult r = oneItem(12.00, 10, "local", true);
        assertEquals(15.0, r.discountPercent, 0.01);
        assertEquals(18.00, r.discount, 0.01);
        assertEquals(109.65, r.total, 0.01);
    }

    // ---------- DELIVERY ----------

    @Test
    void regionalDeliveryCostsTwelve() {
        OrderResult r = oneItem(20.00, 1, "regional", false);
        assertEquals(12.00, r.deliveryFee, 0.01);
        assertEquals(33.50, r.total, 0.01);
    }

    @Test
    void internationalDeliveryIsNeverFree() {
        OrderResult r = oneItem(300.00, 1, "international", false);
        assertEquals(30.00, r.deliveryFee, 0.01);
    }

    // ---------- BOUNDARY CASES ----------

    @Test
    void justBelowFiftyHasNoDiscount() {
        OrderResult r = oneItem(49.99, 1, "local", false);
        assertEquals(0.00, r.discount, 0.01);
    }

    @Test
    void localDeliveryIsFreeAtExactlyEighty() {
        assertEquals(0.00, oneItem(80.00, 1, "local", false).deliveryFee, 0.01);
        assertEquals(5.00, oneItem(79.99, 1, "local", false).deliveryFee, 0.01);
    }

    @Test
    void nineItemsNoBulkDiscountTenItemsYes() {
        assertEquals(0.00, oneItem(1.00, 9, "local", false).discount, 0.01);
        assertEquals(0.30, oneItem(1.00, 10, "local", false).discount, 0.01);
    }

    // ---------- INVALID INPUT ----------

    @Test
    void emptyOrderIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OrderCalculator.calculate(new double[0], new int[0], "local", false));
        assertEquals("Order must contain at least one item.", e.getMessage());
    }

    @Test
    void zeroPriceIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> oneItem(0, 1, "local", false));
        assertEquals("Item 1: price must be greater than 0 and at most 100000.", e.getMessage());
    }

    @Test
    void zeroQuantityIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> oneItem(10.00, 0, "local", false));
        assertEquals("Item 1: quantity must be between 1 and 100.", e.getMessage());
    }

    @Test
    void unknownLocationIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> oneItem(10.00, 1, "mars", false));
        assertEquals("Location must be local, regional or international.", e.getMessage());
    }
}