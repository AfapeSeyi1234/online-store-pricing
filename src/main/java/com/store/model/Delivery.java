package com.store.model;

public class Delivery {

    private final Address address;
    private DeliveryStatus status;

    public Delivery(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Delivery address is required.");
        }
        this.address = address;
        this.status = DeliveryStatus.PREPARING;
    }

    public Address getAddress() {
        return address;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void markInTransit() {
        if (status != DeliveryStatus.PREPARING) {
            throw new IllegalStateException("Only a delivery being prepared can go in transit.");
        }
        status = DeliveryStatus.IN_TRANSIT;
    }

    public void markDelivered() {
        if (status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException("Only a delivery in transit can be delivered.");
        }
        status = DeliveryStatus.DELIVERED;
    }
}