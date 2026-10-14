package com.store.model;

import com.store.exception.InvalidStateException;
import com.store.exception.ValidationException;
import com.store.validation.Validator;

public class Payment {

    private final double amount;
    private final PaymentMethod method;
    private PaymentStatus status;

    public Payment(double amount, PaymentMethod method) {
        if (amount <= 0) {
            throw new ValidationException("Payment amount must be greater than 0.");
        }
        this.amount = amount;
        this.method = Validator.requireNotNull(method, "Payment method");
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
        changeStatus(PaymentStatus.PENDING, PaymentStatus.SUCCESSFUL,
                "Only a pending payment can be marked successful.");
    }

    public void markFailed() {
        changeStatus(PaymentStatus.PENDING, PaymentStatus.FAILED,
                "Only a pending payment can be marked failed.");
    }

    public void refund() {
        changeStatus(PaymentStatus.SUCCESSFUL, PaymentStatus.REFUNDED,
                "Only a successful payment can be refunded.");
    }

    // One place for the "is this change allowed?" check.
    private void changeStatus(PaymentStatus required, PaymentStatus next, String errorMessage) {
        if (status != required) {
            throw new InvalidStateException(errorMessage);
        }
        status = next;
    }
}