package com.store.domain;

public class Product {

    public static final double MAX_PRICE = 100000;

    private final String id;
    private String name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) {
        this.id = Check.text(id, "Product id");
        this.name = Check.text(name, "Product name");
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
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        if (quantity > stock) {
            throw new IllegalArgumentException(
                    "Not enough stock for " + name + ". Available: " + stock + ".");
        }
        stock = stock - quantity;
    }

    public void addStock(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        stock = stock + quantity;
    }

    private static double checkPrice(double price) {
        if (price <= 0 || price > MAX_PRICE) {
            throw new IllegalArgumentException("Price must be greater than 0 and at most 100000.");
        }
        return price;
    }

    private static int checkStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        return stock;
    }
}