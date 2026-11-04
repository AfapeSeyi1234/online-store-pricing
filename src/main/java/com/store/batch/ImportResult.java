package com.store.batch;

import com.store.model.OrderStatus;
import com.store.util.MoneyUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Everything that came out of an import: the accepted and the rejected records, plus the totals.
public record ImportResult(List<ImportedOrder> accepted, List<RejectedRecord> rejected) {

    public ImportResult {
        accepted = List.copyOf(accepted);
        rejected = List.copyOf(rejected);
    }

    public int acceptedCount() {
        return accepted.size();
    }

    public int rejectedCount() {
        return rejected.size();
    }

    public int recordsRead() {
        return accepted.size() + rejected.size();
    }

    public double totalAmount() {
        double sum = 0;
        for (ImportedOrder order : accepted) {
            sum = sum + order.lineTotal();
        }
        return MoneyUtil.round2(sum);
    }

    // 0 when there are no accepted orders (never divides by zero).
    public double averageOrderValue() {
        if (accepted.isEmpty()) {
            return 0;
        }
        return MoneyUtil.round2(totalAmount() / accepted.size());
    }

    // Every status appears in the result, with 0 if no order had it.
    public Map<OrderStatus, Double> totalsByStatus() {
        Map<OrderStatus, Double> totals = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            totals.put(status, 0.0);
        }
        for (ImportedOrder order : accepted) {
            totals.merge(order.status(), order.lineTotal(), Double::sum);
        }
        for (Map.Entry<OrderStatus, Double> entry : totals.entrySet()) {
            entry.setValue(MoneyUtil.round2(entry.getValue()));
        }
        return totals;
    }
}