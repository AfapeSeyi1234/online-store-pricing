package com.store.model;

import com.store.exception.ValidationException;
import com.store.validation.Validator;

public class Product {

    private final String id;
    private String name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) {
        this.id = Validator.requireText(id, "Product id");
        this.name = Validator.requireText(name, "Product name");
        this.price = checkPrice(price);
        this.stock = checkStock(stock);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void changePrice(double newPrice) {
        this.price = checkPrice(newPrice);
    }

    public void reduceStock(int quantity) {
        checkPositive(quantity);
        if (quantity > stock) {
            throw new ValidationException("Not enough stock for " + name + ". Available: " + stock + ".");
        }
        stock = stock - quantity;
    }

    public void addStock(int quantity) {
        checkPositive(quantity);
        stock = stock + quantity;
    }

    private static void checkPositive(int quantity) {
        if (quantity < 1) {
            throw new ValidationException("Quantity must be at least 1.");
        }
    }

    private static double checkPrice(double price) {
        if (!Validator.isValidPrice(price)) {
            throw new ValidationException("Price must be greater than 0 and at most 100000.");
        }
        return price;
    }

    private static int checkStock(int stock) {
        if (stock < 0) {
            throw new ValidationException("Stock cannot be negative.");
        }
        return stock;
    }
}