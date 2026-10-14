package com.store.model;

import com.store.exception.ValidationException;
import com.store.validation.Validator;

// One line of an order. The price is copied from the product when the item is created.
public record OrderItem(Product product, int quantity, double unitPrice) {

    public OrderItem {
        Validator.requireNotNull(product, "Product");
        if (!Validator.isValidQuantity(quantity)) {
            throw new ValidationException("Quantity must be between 1 and 100.");
        }
        if (unitPrice <= 0) {
            throw new ValidationException("Unit price must be greater than 0.");
        }
    }

    public static OrderItem of(Product product, int quantity) {
        Validator.requireNotNull(product, "Product");
        return new OrderItem(product, quantity, product.getPrice());
    }

    public double lineTotal() {
        return unitPrice * quantity;
    }
}