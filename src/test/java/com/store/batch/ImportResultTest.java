package com.store.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.store.model.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ImportResultTest {

    private ImportedOrder order(String id, int quantity, double price, OrderStatus status) {
        return new ImportedOrder(id, "C1", "P1", quantity, price,
                LocalDate.of(2026, 10, 1), LocalDateTime.of(2026, 10, 1, 9, 0), status);
    }

    private ImportResult twoOrders() {
        return new ImportResult(List.of(
                order("O1", 2, 50.00, OrderStatus.PAID),
                order("O2", 1, 25.50, OrderStatus.PAID)), List.of());
    }

    @Test
    void totalIsTheSumOfTheLineTotals() {
        assertEquals(125.50, twoOrders().totalAmount(), 0.001);
    }

    @Test
    void averageOrderValueIsTheTotalDividedByTheCount() {
        assertEquals(62.75, twoOrders().averageOrderValue(), 0.001);
    }

    @Test
    void emptyResultHasZeroTotalAndZeroAverage() {
        ImportResult empty = new ImportResult(List.of(), List.of());
        assertEquals(0.0, empty.totalAmount(), 0.001);
        assertEquals(0.0, empty.averageOrderValue(), 0.001);
        assertEquals(0, empty.recordsRead());
    }

    @Test
    void totalsByStatusAddUpPerStatusAndShowZeroForTheOthers() {
        ImportResult result = new ImportResult(List.of(
                order("O1", 2, 50.00, OrderStatus.PAID),
                order("O2", 1, 25.50, OrderStatus.PAID),
                order("O3", 1, 10.00, OrderStatus.CANCELLED)), List.of());
        assertEquals(125.50, result.totalsByStatus().get(OrderStatus.PAID), 0.001);
        assertEquals(10.00, result.totalsByStatus().get(OrderStatus.CANCELLED), 0.001);
        assertEquals(0.0, result.totalsByStatus().get(OrderStatus.NEW), 0.001);
    }

    @Test
    void recordsReadCountsAcceptedAndRejected() {
        ImportResult result = new ImportResult(
                List.of(order("O1", 1, 10.00, OrderStatus.NEW)),
                List.of(new RejectedRecord(5, "bad", List.of("reason"))));
        assertEquals(2, result.recordsRead());
    }
}