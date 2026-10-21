package com.store.exception;

public class ProductNotFoundException extends NotFoundException {

    private final String productId;

    public ProductNotFoundException(String productId) {
        super("Product " + productId + " was not found.");
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}