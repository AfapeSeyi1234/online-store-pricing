package com.store.model;

import com.store.exception.ValidationException;
import com.store.validation.Validator;

// Immutable. The checks run every time an Address is created.
public record Address(String street, String city, String country, String zone) {

    public Address {
        street = Validator.requireText(street, "Street");
        city = Validator.requireText(city, "City");
        country = Validator.requireText(country, "Country");
        if (!Validator.isValidLocation(zone)) {
            throw new ValidationException("Zone must be local, regional or international.");
        }
        zone = zone.toLowerCase();
    }
}