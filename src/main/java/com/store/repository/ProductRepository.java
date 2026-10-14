package com.store.repository;

import com.store.model.Product;

import java.util.List;
import java.util.Optional;

// Where products are stored. Today in memory; later it could be a database.
public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(String id);

    List<Product> findAll();
}