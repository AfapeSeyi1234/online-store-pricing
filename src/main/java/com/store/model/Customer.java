package com.store.model;

import com.store.validation.Validator;

public class Customer {

    private final String id;
    private String name;
    private String email;
    private Address address;
    private boolean loyaltyMember;

    public Customer(String id, String name, String email, Address address) {
        this.id = Validator.requireText(id, "Customer id");
        this.name = Validator.requireText(name, "Customer name");
        this.email = Validator.requireEmail(email);
        this.address = Validator.requireNotNull(address, "Address");
        this.loyaltyMember = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isLoyaltyMember() {
        return loyaltyMember;
    }

    public void changeAddress(Address newAddress) {
        this.address = Validator.requireNotNull(newAddress, "Address");
    }

    public void makeLoyaltyMember() {
        this.loyaltyMember = true;
    }
}