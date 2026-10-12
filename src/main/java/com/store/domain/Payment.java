package com.store.domain;

public class Payment {

    private final double amount;
    private final PaymentMethod method;
    private PaymentStatus status;

    public Payment(double amount, PaymentMethod method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0.");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void markSuccessful() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only a pending payment can be marked successful.");
        }
        status = PaymentStatus.SUCCESSFUL;
    }

    public void markFailed() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only a pending payment can be marked failed.");
        }
        status = PaymentStatus.FAILED;
    }

    public void refund() {
        if (status != PaymentStatus.SUCCESSFUL) {
            throw new IllegalStateException("Only a successful payment can be refunded.");
        }
        status = PaymentStatus.REFUNDED;
    }
}