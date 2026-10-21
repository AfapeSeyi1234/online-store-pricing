package com.store.analytics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class SalesReportTest {

    @Test
    void reportShowsTheMainFigures() {
        OrderAnalyser analyser = new OrderAnalyser(SampleData.create().getOrders());
        String report = SalesReport.create(analyser);
        assertTrue(report.contains("Orders in total: 8"));
        assertTrue(report.contains("Cancelled orders: 1"));
        assertTrue(report.contains("Total sales: 866.13"));
        assertTrue(report.contains("Pen - 30 units"));
    }

    @Test
    void reportForNoOrdersShowsZerosAndNoSalesMessage() {
        String report = SalesReport.create(new OrderAnalyser(List.of()));
        assertTrue(report.contains("Orders in total: 0"));
        assertTrue(report.contains("Total sales: 0.00"));
        assertTrue(report.contains("No sales yet."));
    }

    @Test
    void reportListsEveryOrderStatus() {
        String report = SalesReport.create(new OrderAnalyser(SampleData.create().getOrders()));
        assertTrue(report.contains("DELIVERED"));
        assertTrue(report.contains("CANCELLED"));
    }
}