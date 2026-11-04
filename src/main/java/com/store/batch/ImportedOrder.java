package com.store.batch;

import com.store.model.OrderStatus;
import com.store.util.MoneyUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

// One accepted order record. A record never changes after it is created.
public record ImportedOrder(String orderId,
                            String customerId,
                            String productId,
                            int quantity,
                            double unitPrice,
                            LocalDate orderDate,
                            LocalDateTime createdAt,
                            OrderStatus status) {

    public double lineTotal() {
        return MoneyUtil.round2(quantity * unitPrice);
    }
}