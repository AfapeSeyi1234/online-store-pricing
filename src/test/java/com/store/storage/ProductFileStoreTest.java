package com.store.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.store.analytics.LoadResult;
import com.store.exception.DataFileException;
import com.store.exception.ValidationException;
import com.store.model.Product;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ProductFileStoreTest {

    @TempDir
    Path tempDir; // JUnit gives every test a fresh temporary folder and deletes it afterwards

    private List<Product> sample() {
        return List.of(
                new Product("P1", "Notebook", 50.00, 100),
                new Product("P2", "Pen", 2.50, 500));
    }

    @Test
    void savedProductsCanBeLoadedAgain() throws DataFileException {
        Path file = tempDir.resolve("products.csv");
        ProductFileStore.save(sample(), file);

        LoadResult<Product> result = ProductFileStore.load(file);

        assertEquals(2, result.items().size());
        assertEquals("Pen", result.items().get(1).getName());
        assertEquals(2.50, result.items().get(1).getPrice(), 0.001);
        assertFalse(result.hasErrors());
    }

    @Test
    void loadingAMissingFileThrowsDataFileException() {
        DataFileException e = assertThrows(DataFileException.class,
                () -> ProductFileStore.load(tempDir.resolve("nothing.csv")));
        assertEquals("File not found: nothing.csv", e.getMessage());
    }

    @Test
    void savingToAFolderThatDoesNotExistThrowsDataFileException() {
        Path file = tempDir.resolve("no-such-folder").resolve("products.csv");
        assertThrows(DataFileException.class, () -> ProductFileStore.save(sample(), file));
    }

    @Test
    void badLinesAreReportedAndTheGoodOnesAreStillLoaded() throws Exception {
        Path file = tempDir.resolve("products.csv");
        Files.write(file, List.of(
                "id,name,price,stock",
                "P1,Notebook,50.00,100",
                "P2,Pen,abc,5",
                "P3,Lamp,35.00,-1"));

        LoadResult<Product> result = ProductFileStore.load(file);

        assertEquals(1, result.items().size());
        assertEquals(2, result.errors().size());
    }

    @Test
    void savingAnEmptyListWritesOnlyTheHeader() throws Exception {
        Path file = tempDir.resolve("products.csv");
        ProductFileStore.save(List.of(), file);
        assertEquals(List.of("id,name,price,stock"), Files.readAllLines(file));
    }

    @Test
    void loadingAnEmptyFileGivesNoProductsAndNoErrors() throws Exception {
        Path file = tempDir.resolve("products.csv");
        Files.write(file, new byte[0]);

        LoadResult<Product> result = ProductFileStore.load(file);

        assertEquals(0, result.items().size());
        assertFalse(result.hasErrors());
    }

    @Test
    void savingAProductNameWithACommaIsRejected() {
        Path file = tempDir.resolve("products.csv");
        List<Product> products = List.of(new Product("P1", "Pens, blue", 2.50, 5));
        assertThrows(ValidationException.class, () -> ProductFileStore.save(products, file));
    }
}