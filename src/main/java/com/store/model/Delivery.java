package com.store.model;

import com.store.exception.InvalidStateException;
import com.store.validation.Validator;

public class Delivery {

    private final Address address;
    private DeliveryStatus status;

    public Delivery(Address address) {
        this.address = Validator.requireNotNull(address, "Delivery address");
        this.status = DeliveryStatus.PREPARING;
    }

    public Address getAddress() {
        return address;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void markInTransit() {
        changeStatus(DeliveryStatus.PREPARING, DeliveryStatus.IN_TRANSIT,
                "Only a delivery being prepared can go in transit.");
    }

    public void markDelivered() {
        changeStatus(DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELIVERED,
                "Only a delivery in transit can be delivered.");
    }

    private void changeStatus(DeliveryStatus required, DeliveryStatus next, String errorMessage) {
        if (status != required) {
            throw new InvalidStateException(errorMessage);
        }
        status = next;
    }
}