package com.store.model;

import com.store.OrderCalculator;

// A record is immutable: once created, an Address never changes.
// The checks in the "compact constructor" run every time one is created.
public record Address(String street, String city, String country, String zone) {

    public Address {
        street = Check.text(street, "Street");
        city = Check.text(city, "City");
        country = Check.text(country, "Country");
        if (!OrderCalculator.isValidLocation(zone)) {
            throw new IllegalArgumentException("Zone must be local, regional or international.");
        }
        zone = zone.toLowerCase();
    }
}