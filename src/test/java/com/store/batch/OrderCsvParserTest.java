package com.store.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.model.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OrderCsvParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
    private static final String HEADER =
            "orderId,customerId,productId,quantity,unitPrice,orderDate,createdAt,status";
    private static final String GOOD = "O1,C1,P1,2,50.00,2026-10-01,2026-10-01T09:15:00,DELIVERED";

    private ImportResult parse(String... lines) {
        return OrderCsvParser.parse(List.of(lines), TODAY);
    }

    private static String line(String id, String quantity, String price) {
        return id + ",C1,P1," + quantity + "," + price + ",2026-10-01,2026-10-01T09:00:00,NEW";
    }

    private static String firstReason(ImportResult result) {
        return result.rejected().get(0).reasons().get(0);
    }

    // ---------- valid records ----------

    @Test
    void validLineIsAcceptedWithAllFieldsParsed() {
        ImportResult result = parse(GOOD);
        assertEquals(1, result.acceptedCount());
        assertEquals(0, result.rejectedCount());
        ImportedOrder order = result.accepted().get(0);
        assertEquals("O1", order.orderId());
        assertEquals("C1", order.customerId());
        assertEquals(2, order.quantity());
        assertEquals(50.00, order.unitPrice(), 0.001);
        assertEquals(LocalDate.of(2026, 10, 1), order.orderDate());
        assertEquals(LocalDateTime.of(2026, 10, 1, 9, 15, 0), order.createdAt());
        assertEquals(OrderStatus.DELIVERED, order.status());
        assertEquals(100.00, order.lineTotal(), 0.001);
    }

    @Test
    void headerLineIsSkipped() {
        ImportResult result = parse(HEADER, GOOD);
        assertEquals(1, result.acceptedCount());
        assertEquals(0, result.rejectedCount());
    }

    @Test
    void blankLinesAreIgnored() {
        ImportResult result = parse(GOOD, "", "   ");
        assertEquals(1, result.recordsRead());
    }

    @Test
    void emptyInputGivesAnEmptyResult() {
        ImportResult result = OrderCsvParser.parse(List.of(), TODAY);
        assertEquals(0, result.recordsRead());
    }

    @Test
    void headerOnlyFileGivesAnEmptyResult() {
        assertEquals(0, parse(HEADER).recordsRead());
    }

    @Test
    void statusIsAcceptedInLowerCase() {
        ImportResult result = parse("O1,C1,P1,1,10.00,2026-10-01,2026-10-01T09:00:00,paid");
        assertEquals(OrderStatus.PAID, result.accepted().get(0).status());
    }

    @Test
    void anInvisibleMarkerAtTheStartOfTheFileIsRemoved() {
        ImportResult result = parse("\uFEFF" + HEADER, GOOD);
        assertEquals(1, result.acceptedCount());
        assertEquals(0, result.rejectedCount());
    }

    // ---------- dates ----------

    @Test
    void malformedOrderDateIsRejectedWithAClearReason() {
        ImportResult result = parse("O2,C1,P1,2,50.00,2026-13-45,2026-10-01T09:15:00,PAID");
        assertEquals(1, result.rejectedCount());
        assertTrue(firstReason(result).contains("orderDate '2026-13-45' is not a valid date"));
    }

    @Test
    void aDateThatDoesNotExistIsRejected() {
        ImportResult result = parse("O2,C1,P1,2,50.00,2026-02-30,2026-02-28T09:15:00,PAID");
        assertEquals(1, result.rejectedCount());
    }

    @Test
    void malformedTimestampIsRejected() {
        ImportResult result = parse("O2,C1,P1,2,50.00,2026-10-01,2026-10-01 09:15,PAID");
        assertEquals(1, result.rejectedCount());
        assertTrue(firstReason(result).contains("createdAt"));
    }

    @Test
    void timestampOnADifferentDayThanTheOrderDateIsRejected() {
        ImportResult result = parse("O2,C1,P1,2,50.00,2026-10-01,2026-10-02T09:15:00,PAID");
        assertTrue(firstReason(result).contains("does not match"));
    }

    @Test
    void aFutureOrderDateIsRejected() {
        ImportResult result = parse("O2,C1,P1,1,50.00,2026-11-03,2026-11-03T09:00:00,NEW");
        assertTrue(firstReason(result).contains("in the future"));
    }

    @Test
    void anOrderDatedTodayIsAccepted() {
        ImportResult result = parse("O2,C1,P1,1,50.00,2026-11-02,2026-11-02T09:00:00,NEW");
        assertEquals(1, result.acceptedCount());
    }

    // ---------- missing and invalid fields ----------

    @Test
    void missingCustomerIdIsRejected() {
        ImportResult result = parse("O3,,P1,2,50.00,2026-10-01,2026-10-01T09:15:00,PAID");
        assertEquals("customerId is required.", firstReason(result));
    }

    @Test
    void wrongNumberOfColumnsIsRejected() {
        ImportResult result = parse("O4,C1,P1,2");
        assertEquals("expected 8 values but found 4.", firstReason(result));
    }

    @Test
    void quantityBoundaries() {
        ImportResult result = parse(
                line("A1", "1", "10.00"), line("A2", "100", "10.00"),
                line("A3", "0", "10.00"), line("A4", "101", "10.00"), line("A5", "two", "10.00"));
        assertEquals(2, result.acceptedCount());
        assertEquals(3, result.rejectedCount());
    }

    @Test
    void priceBoundaries() {
        ImportResult result = parse(
                line("B1", "1", "0.01"), line("B2", "1", "100000"),
                line("B3", "1", "0"), line("B4", "1", "100000.01"), line("B5", "1", "abc"));
        assertEquals(2, result.acceptedCount());
        assertEquals(3, result.rejectedCount());
        assertTrue(result.rejected().get(2).reasons().get(0).contains("is not a number"));
    }

    @Test
    void unknownStatusIsRejected() {
        ImportResult result = parse("O5,C1,P1,1,10.00,2026-10-01,2026-10-01T09:00:00,RETURNED");
        assertTrue(firstReason(result).contains("status 'RETURNED' is not valid"));
    }

    // ---------- duplicates and reporting ----------

    @Test
    void duplicateOrderIdIsRejectedAndTheFirstOneIsKept() {
        ImportResult result = parse(GOOD, GOOD);
        assertEquals(1, result.acceptedCount());
        assertEquals(1, result.rejectedCount());
        assertTrue(firstReason(result).contains("duplicate orderId O1"));
        assertTrue(firstReason(result).contains("first seen on line 1"));
    }

    @Test
    void rejectedRecordKeepsItsLineNumberAndOriginalText() {
        String bad = "O6,C1,P1,0,10.00,2026-10-01,2026-10-01T09:00:00,NEW";
        ImportResult result = parse(HEADER, GOOD, bad);
        assertEquals(3, result.rejected().get(0).lineNumber());
        assertEquals(bad, result.rejected().get(0).rawLine());
    }

    @Test
    void everyProblemInOneRecordIsReported() {
        ImportResult result = parse("O9,,P1,0,-1,bad,bad,wrong");
        assertEquals(6, result.rejected().get(0).reasons().size());
    }
}