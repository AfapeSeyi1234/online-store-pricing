package com.store.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.model.Product;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ProductCsvLoaderTest {

    @Test
    void validLinesBecomeProducts() {
        LoadResult<Product> result = ProductCsvLoader.load(
                List.of("P1,Notebook,50.00,100", "P2,Pen,2.50,500"));
        assertEquals(2, result.items().size());
        assertEquals("Notebook", result.items().get(0).getName());
        assertFalse(result.hasErrors());
    }

    @Test
    void headerAndBlankLinesAreIgnored() {
        LoadResult<Product> result = ProductCsvLoader.load(
                List.of("id,name,price,stock", "P1,Notebook,50.00,100", "", "   "));
        assertEquals(1, result.items().size());
        assertFalse(result.hasErrors());
    }

    @Test
    void extraSpacesAroundValuesAreTrimmed() {
        LoadResult<Product> result = ProductCsvLoader.load(List.of(" P1 , Notebook , 50.00 , 100 "));
        assertEquals("P1", result.items().get(0).getId());
        assertEquals("Notebook", result.items().get(0).getName());
    }

    @Test
    void priceThatIsNotANumberIsReported() {
        LoadResult<Product> result = ProductCsvLoader.load(List.of("P1,Notebook,abc,100"));
        assertTrue(result.items().isEmpty());
        assertEquals("Line 1: price 'abc' is not a number.", result.errors().get(0));
    }

    @Test
    void negativeStockIsReported() {
        LoadResult<Product> result = ProductCsvLoader.load(List.of("P1,Notebook,50.00,-5"));
        assertEquals("Line 1: Stock cannot be negative.", result.errors().get(0));
    }

    @Test
    void zeroPriceIsReportedButTheMaximumPriceIsAccepted() {
        LoadResult<Product> zero = ProductCsvLoader.load(List.of("P1,Notebook,0,10"));
        assertEquals(1, zero.errors().size());

        LoadResult<Product> max = ProductCsvLoader.load(List.of("P1,Notebook,100000,10"));
        assertEquals(1, max.items().size());

        LoadResult<Product> tooHigh = ProductCsvLoader.load(List.of("P1,Notebook,100001,10"));
        assertEquals(1, tooHigh.errors().size());
    }

    @Test
    void wrongNumberOfColumnsIsReported() {
        LoadResult<Product> result = ProductCsvLoader.load(List.of("P1,Notebook,50.00"));
        assertEquals("Line 1: expected 4 values (id, name, price, stock) but found 3.",
                result.errors().get(0));
    }

    @Test
    void duplicateIdIsReportedAndTheFirstOneIsKept() {
        LoadResult<Product> result = ProductCsvLoader.load(
                List.of("P1,Notebook,50.00,100", "P1,Other,10.00,5"));
        assertEquals(1, result.items().size());
        assertEquals("Notebook", result.items().get(0).getName());
        assertEquals("Line 2: duplicate product id P1.", result.errors().get(0));
    }

    @Test
    void emptyInputGivesEmptyResult() {
        LoadResult<Product> result = ProductCsvLoader.load(List.of());
        assertTrue(result.items().isEmpty());
        assertFalse(result.hasErrors());
    }

    @Test
    void sampleLinesGiveThreeProductsAndFourErrors() {
        LoadResult<Product> result = ProductCsvLoader.load(SampleData.productCsvLines());
        assertEquals(3, result.items().size());
        assertEquals(4, result.errors().size());
    }
}