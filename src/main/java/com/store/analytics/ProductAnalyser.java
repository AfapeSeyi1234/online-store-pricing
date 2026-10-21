package com.store.analytics;

import com.store.model.Product;
import com.store.util.CollectionUtil;
import com.store.validation.Validator;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProductAnalyser {

    private final List<Product> products;

    public ProductAnalyser(List<Product> products) {
        Validator.requireNotNull(products, "Products");
        this.products = List.copyOf(products);
    }

    // Finds products whose name or id contains the text (upper/lower case does not matter).
    public List<Product> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String needle = text.trim().toLowerCase();
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(needle)
                        || p.getId().toLowerCase().contains(needle))
                .toList();
    }

    public List<Product> sortByName() {
        Comparator<Product> byName = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        return CollectionUtil.sortedCopy(products, byName);
    }

    public List<Product> sortByPrice(boolean cheapestFirst) {
        Comparator<Product> byPrice = Comparator.comparingDouble(Product::getPrice);
        return CollectionUtil.sortedCopy(products, cheapestFirst ? byPrice : byPrice.reversed());
    }

    public List<Product> sortByStock(boolean lowestFirst) {
        Comparator<Product> byStock = Comparator.comparingInt(Product::getStock);
        return CollectionUtil.sortedCopy(products, lowestFirst ? byStock : byStock.reversed());
    }

    public Optional<Product> mostExpensive() {
        Comparator<Product> byPrice = Comparator.comparingDouble(Product::getPrice);
        return CollectionUtil.maxBy(products, byPrice);
    }

    public Optional<Product> cheapest() {
        Comparator<Product> byPrice = Comparator.comparingDouble(Product::getPrice);
        return CollectionUtil.minBy(products, byPrice);
    }

    // Names that appear more than once (compared in lower case).
    public Set<String> findDuplicateNames() {
        return CollectionUtil.findDuplicateKeys(products, p -> p.getName().toLowerCase());
    }

    public List<Product> removeDuplicateNames() {
        return CollectionUtil.removeDuplicates(products, p -> p.getName().toLowerCase());
    }

    // A Map gives fast lookup by id. If an id appears twice, the first product wins.
    public Map<String, Product> productsById() {
        Map<String, Product> lookup = new LinkedHashMap<>();
        for (Product product : products) {
            lookup.putIfAbsent(product.getId(), product);
        }
        return lookup;
    }
}