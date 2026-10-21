package com.store.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.exception.ValidationException;
import com.store.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ProductAnalyserTest {

    private List<Product> sampleProducts() {
        return List.of(
                new Product("P1", "Notebook", 50.00, 100),
                new Product("P2", "pen", 2.50, 500),
                new Product("P3", "Backpack", 120.00, 20));
    }

    private List<String> names(List<Product> products) {
        return products.stream().map(Product::getName).toList();
    }

    @Test
    void searchFindsProductsIgnoringCase() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertEquals(List.of("pen"), names(analyser.search("PEN")));
        assertEquals(List.of("Backpack"), names(analyser.search("back")));
    }

    @Test
    void searchCanFindByProductId() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertEquals(List.of("Notebook"), names(analyser.search("p1")));
    }

    @Test
    void searchWithNoMatchGivesEmptyList() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertTrue(analyser.search("zzz").isEmpty());
    }

    @Test
    void blankOrNullSearchGivesEmptyList() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertTrue(analyser.search("  ").isEmpty());
        assertTrue(analyser.search(null).isEmpty());
    }

    @Test
    void sortByNameIgnoresUpperAndLowerCase() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertEquals(List.of("Backpack", "Notebook", "pen"), names(analyser.sortByName()));
    }

    @Test
    void sortByPriceWorksBothWays() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertEquals(List.of("pen", "Notebook", "Backpack"), names(analyser.sortByPrice(true)));
        assertEquals(List.of("Backpack", "Notebook", "pen"), names(analyser.sortByPrice(false)));
    }

    @Test
    void sortByStockWorksBothWays() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertEquals(List.of("Backpack", "Notebook", "pen"), names(analyser.sortByStock(true)));
        assertEquals(List.of("pen", "Notebook", "Backpack"), names(analyser.sortByStock(false)));
    }

    @Test
    void sortingAnEmptyListGivesAnEmptyList() {
        ProductAnalyser analyser = new ProductAnalyser(List.of());
        assertTrue(analyser.sortByName().isEmpty());
        assertTrue(analyser.sortByPrice(true).isEmpty());
    }

    @Test
    void mostExpensiveAndCheapestFindTheExtremes() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertEquals("Backpack", analyser.mostExpensive().get().getName());
        assertEquals("pen", analyser.cheapest().get().getName());
    }

    @Test
    void extremesOfAnEmptyListAreEmpty() {
        ProductAnalyser analyser = new ProductAnalyser(List.of());
        assertTrue(analyser.mostExpensive().isEmpty());
        assertTrue(analyser.cheapest().isEmpty());
    }

    @Test
    void duplicateNamesAreFound() {
        List<Product> products = new ArrayList<>(sampleProducts());
        products.add(new Product("P9", "NOTEBOOK", 10.00, 5));
        ProductAnalyser analyser = new ProductAnalyser(products);
        assertEquals(Set.of("notebook"), analyser.findDuplicateNames());
    }

    @Test
    void removeDuplicateNamesKeepsTheFirstProduct() {
        List<Product> products = new ArrayList<>(sampleProducts());
        products.add(new Product("P9", "NOTEBOOK", 10.00, 5));
        ProductAnalyser analyser = new ProductAnalyser(products);
        List<Product> unique = analyser.removeDuplicateNames();
        assertEquals(3, unique.size());
        assertEquals("P1", unique.get(0).getId());
    }

    @Test
    void productsByIdGivesFastLookup() {
        ProductAnalyser analyser = new ProductAnalyser(sampleProducts());
        assertEquals("Backpack", analyser.productsById().get("P3").getName());
        assertTrue(analyser.productsById().get("P99") == null);
    }

    @Test
    void nullListIsRejected() {
        assertThrows(ValidationException.class, () -> new ProductAnalyser(null));
    }
}