package com.store.storage;

import com.store.analytics.LoadResult;
import com.store.analytics.ProductCsvLoader;
import com.store.exception.DataFileException;
import com.store.exception.ValidationException;
import com.store.model.Product;
import com.store.validation.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

// Saves and loads products as a CSV file.
// try-with-resources closes the reader or writer automatically, even if an error happens.
public final class ProductFileStore {

    private static final Logger LOGGER = Logger.getLogger(ProductFileStore.class.getName());
    private static final String HEADER = "id,name,price,stock";

    private ProductFileStore() {
    }

    public static void save(List<Product> products, Path file) throws DataFileException {
        Validator.requireNotNull(products, "Products");
        Validator.requireNotNull(file, "File path");
        for (Product product : products) {
            if (product.getName().contains(",")) {
                throw new ValidationException("Product name cannot contain a comma: " + product.getName());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Product p : products) {
                writer.write(p.getId() + "," + p.getName() + ","
                        + String.format(Locale.US, "%.2f", p.getPrice()) + "," + p.getStock());
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not write product file " + file.getFileName(), e);
            throw new DataFileException("Could not write file " + file.getFileName()
                    + " (" + e.getClass().getSimpleName() + ").", e);
        }
        LOGGER.info("Saved " + products.size() + " products to " + file.getFileName());
    }

    // Bad lines are skipped and reported in the result, the same way as ProductCsvLoader does.
    public static LoadResult<Product> load(Path file) throws DataFileException {
        Validator.requireNotNull(file, "File path");
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (NoSuchFileException e) {
            LOGGER.warning("Product file not found: " + file.getFileName());
            throw new DataFileException("File not found: " + file.getFileName(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read product file " + file.getFileName(), e);
            throw new DataFileException("Could not read file " + file.getFileName()
                    + " (" + e.getClass().getSimpleName() + ").", e);
        }

        LoadResult<Product> result = ProductCsvLoader.load(lines);
        LOGGER.info("Loaded " + result.items().size() + " products from " + file.getFileName()
                + ", " + result.errors().size() + " bad line(s) skipped");
        for (String error : result.errors()) {
            LOGGER.warning("Product file problem: " + error);
        }
        return result;
    }
}