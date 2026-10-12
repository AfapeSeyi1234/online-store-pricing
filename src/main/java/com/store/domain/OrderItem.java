package com.store.domain;

// One line of an order. The price is copied from the product when the item is created,
// so a later price change does not alter an existing order.
public record OrderItem(Product product, int quantity, double unitPrice) {

    public OrderItem {
        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }
        if (quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException("Quantity must be between 1 and 100.");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than 0.");
        }
    }

    public static OrderItem of(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }
        return new OrderItem(product, quantity, product.getPrice());
    }

    public double lineTotal() {
        return unitPrice * quantity;
    }
}