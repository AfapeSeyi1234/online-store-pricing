package com.store.model;

public class Customer {

    private final String id;
    private String name;
    private String email;
    private Address address;
    private boolean loyaltyMember;

    public Customer(String id, String name, String email, Address address) {
        this.id = Check.text(id, "Customer id");
        this.name = Check.text(name, "Customer name");
        this.email = checkEmail(email);
        if (address == null) {
            throw new IllegalArgumentException("Address is required.");
        }
        this.address = address;
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
        if (newAddress == null) {
            throw new IllegalArgumentException("Address is required.");
        }
        this.address = newAddress;
    }

    public void makeLoyaltyMember() {
        this.loyaltyMember = true;
    }

    private static String checkEmail(String email) {
        String text = Check.text(email, "Email");
        if (!text.contains("@") || !text.contains(".")) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        return text;
    }
}