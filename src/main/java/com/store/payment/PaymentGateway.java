package com.store.payment;

public interface PaymentGateway {

    // Charges the amount. Throws PaymentFailedException if the payment is not accepted.
    // Only the order id and amount are passed, never card or account details.
    void charge(String orderId, double amount);
}