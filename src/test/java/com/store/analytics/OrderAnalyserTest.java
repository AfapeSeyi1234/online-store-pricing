package com.store.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.exception.ValidationException;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.OrderStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderAnalyserTest {

    private List<Order> orders;
    private OrderAnalyser analyser;

    @BeforeEach
    void setUp() {
        orders = SampleData.create().getOrders();
        analyser = new OrderAnalyser(orders);
    }

    private List<String> ids(List<Order> list) {
        return list.stream().map(Order::getId).toList();
    }

    // ---------- search ----------

    @Test
    void searchFindsOrdersByCustomerName() {
        assertEquals(List.of("O1", "O4", "O8"), ids(analyser.search("ada")));
    }

    @Test
    void searchFindsOrdersByIdIgnoringCase() {
        assertEquals(List.of("O1"), ids(analyser.search("o1")));
    }

    @Test
    void searchWithNoMatchGivesEmptyList() {
        assertTrue(analyser.search("zzz").isEmpty());
        assertTrue(analyser.search("").isEmpty());
        assertTrue(analyser.search(null).isEmpty());
    }

    // ---------- filters ----------

    @Test
    void filterByStatusFindsMatchingOrders() {
        assertEquals(List.of("O1", "O6"), ids(analyser.filterByStatus(OrderStatus.DELIVERED)));
    }

    @Test
    void filterByStatusOnAnEmptyAnalyserGivesEmptyList() {
        OrderAnalyser empty = new OrderAnalyser(List.of());
        assertTrue(empty.filterByStatus(OrderStatus.PAID).isEmpty());
    }

    @Test
    void filterByCustomerFindsThatCustomersOrders() {
        assertEquals(List.of("O1", "O4", "O8"), ids(analyser.filterByCustomer("C1")));
    }

    @Test
    void filterByUnknownCustomerGivesEmptyList() {
        assertTrue(analyser.filterByCustomer("C99").isEmpty());
    }

    @Test
    void filterByDateIncludesTheFirstAndLastDay() {
        List<Order> found = analyser.filterByDate(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12));
        assertEquals(List.of("O3", "O4", "O5", "O6"), ids(found));
    }

    @Test
    void filterByASingleDayWorks() {
        List<Order> found = analyser.filterByDate(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 10));
        assertEquals(List.of("O5"), ids(found));
    }

    @Test
    void invalidDateRangeIsRejected() {
        assertThrows(ValidationException.class,
                () -> analyser.filterByDate(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 5)));
        assertThrows(ValidationException.class, () -> analyser.filterByDate(null, null));
    }

    @Test
    void filterByValueFindsOrdersInRange() {
        assertEquals(List.of("O7"), ids(analyser.filterByValue(200, 300)));
        assertEquals(8, analyser.filterByValue(0, 10000).size());
    }

    @Test
    void filterByValueWhenMinimumEqualsMaximum() {
        assertEquals(List.of("O7"), ids(analyser.filterByValue(232.2, 232.2)));
    }

    @Test
    void invalidValueRangeIsRejected() {
        assertThrows(ValidationException.class, () -> analyser.filterByValue(-1, 5));
        assertThrows(ValidationException.class, () -> analyser.filterByValue(300, 200));
    }

    // ---------- grouping and counting ----------

    @Test
    void groupByStatusPutsOrdersInTheRightGroups() {
        Map<OrderStatus, List<Order>> groups = analyser.groupByStatus();
        assertEquals(5, groups.size());
        assertEquals(2, groups.get(OrderStatus.DELIVERED).size());
        assertEquals(1, groups.get(OrderStatus.NEW).size());
        assertEquals(1, groups.get(OrderStatus.CANCELLED).size());
    }

    @Test
    void groupByCustomerPutsOrdersInTheRightGroups() {
        Map<String, List<Order>> groups = analyser.groupByCustomer();
        assertEquals(4, groups.size());
        assertEquals(List.of("O1", "O4", "O8"), ids(groups.get("C1")));
        assertEquals(1, groups.get("C4").size());
    }

    @Test
    void groupingAnEmptyListGivesAnEmptyMap() {
        OrderAnalyser empty = new OrderAnalyser(List.of());
        assertTrue(empty.groupByStatus().isEmpty());
        assertTrue(empty.groupByCustomer().isEmpty());
        assertTrue(empty.countByStatus().isEmpty());
    }

    @Test
    void countByStatusCountsEveryStatus() {
        Map<OrderStatus, Long> counts = analyser.countByStatus();
        assertEquals(1L, counts.get(OrderStatus.NEW).longValue());
        assertEquals(2L, counts.get(OrderStatus.PAID).longValue());
        assertEquals(2L, counts.get(OrderStatus.SHIPPED).longValue());
        assertEquals(2L, counts.get(OrderStatus.DELIVERED).longValue());
        assertEquals(1L, counts.get(OrderStatus.CANCELLED).longValue());
    }

    // ---------- sales figures ----------

    @Test
    void onlyPaidShippedAndDeliveredOrdersAreSales() {
        assertEquals(List.of("O1", "O2", "O3", "O6", "O7", "O8"), ids(analyser.salesOrders()));
    }

    @Test
    void totalSalesIgnoresNewAndCancelledOrders() {
        assertEquals(866.13, analyser.totalSales(), 0.01);
    }

    @Test
    void averageOrderValueIsTotalDividedBySalesCount() {
        assertEquals(144.36, analyser.averageOrderValue(), 0.02);
    }

    @Test
    void salesByCustomerAddsUpEachCustomersSales() {
        Map<String, Double> sales = analyser.salesByCustomer();
        assertEquals(3, sales.size());
        assertEquals(223.83, sales.get("Ada Obi"), 0.01);
        assertTrue(!sales.containsKey("John Smith")); // his only order was cancelled
    }

    @Test
    void bestSellingProductsAreSortedByUnitsSold() {
        List<Map.Entry<String, Integer>> top = analyser.bestSellingProducts(3);
        assertEquals(3, top.size());
        assertEquals("Pen", top.get(0).getKey());
        assertEquals(30, top.get(0).getValue().intValue());
        assertEquals("USB Cable", top.get(1).getKey());
        assertEquals("Notebook", top.get(2).getKey());
    }

    @Test
    void bestSellingLimitLargerThanTheNumberOfProductsReturnsAll() {
        assertEquals(5, analyser.bestSellingProducts(100).size());
    }

    @Test
    void bestSellingLimitBelowOneIsRejected() {
        assertThrows(ValidationException.class, () -> analyser.bestSellingProducts(0));
    }

    @Test
    void emptyAnalyserGivesZeroSalesAndNoBestSellers() {
        OrderAnalyser empty = new OrderAnalyser(List.of());
        assertEquals(0.0, empty.totalSales(), 0.001);
        assertEquals(0.0, empty.averageOrderValue(), 0.001);
        assertTrue(empty.bestSellingProducts(3).isEmpty());
        assertTrue(empty.search("ada").isEmpty());
        assertTrue(empty.customersWhoOrdered().isEmpty());
    }

    // ---------- duplicates and sets ----------

    @Test
    void duplicateOrderIdsAreFoundAndRemoved() {
        Customer customer = orders.get(0).getCustomer();
        List<Order> withDuplicate = new ArrayList<>(orders);
        withDuplicate.add(new Order("O1", customer));
        OrderAnalyser duplicated = new OrderAnalyser(withDuplicate);

        assertEquals(Set.of("O1"), duplicated.findDuplicateOrderIds());
        List<Order> unique = duplicated.removeDuplicateOrders();
        assertEquals(8, unique.size());
        assertSame(orders.get(0), unique.get(0));
    }

    @Test
    void customersWhoOrderedAreUniqueAndSorted() {
        assertEquals(List.of("Ada Obi", "Chioma Eze", "John Smith", "Tunde Bello"),
                new ArrayList<>(analyser.customersWhoOrdered()));
    }

    @Test
    void nullOrderListIsRejected() {
        assertThrows(ValidationException.class, () -> new OrderAnalyser(null));
    }
}