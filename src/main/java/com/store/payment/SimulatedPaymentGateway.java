package com.store.payment;

import com.store.exception.PaymentFailedException;
import com.store.exception.ValidationException;

// Pretend payment provider: it declines any amount above its limit.
public class SimulatedPaymentGateway implements PaymentGateway {

    private final double limit;

    public SimulatedPaymentGateway(double limit) {
        if (limit <= 0) {
            throw new ValidationException("Limit must be greater than 0.");
        }
        this.limit = limit;
    }

    public static SimulatedPaymentGateway alwaysApprove() {
        return new SimulatedPaymentGateway(Double.MAX_VALUE);
    }

    @Override
    public void charge(String orderId, double amount) {
        if (amount > limit) {
            throw new PaymentFailedException("Payment declined: amount is above the allowed limit.");
        }
    }
}