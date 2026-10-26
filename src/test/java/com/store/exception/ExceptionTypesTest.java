package com.store.exception;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ExceptionTypesTest {

    @Test
    void dataFileExceptionIsCheckedSoCallersMustHandleIt() {
        assertTrue(Exception.class.isAssignableFrom(DataFileException.class));
        assertFalse(RuntimeException.class.isAssignableFrom(DataFileException.class));
    }

    @Test
    void paymentFailedExceptionIsUnchecked() {
        assertTrue(RuntimeException.class.isAssignableFrom(PaymentFailedException.class));
    }

    @Test
    void orderNotFoundExceptionIsAlsoANotFoundException() {
        assertTrue(NotFoundException.class.isAssignableFrom(OrderNotFoundException.class));
    }

    @Test
    void invalidOrderStateExceptionIsAlsoAnIllegalStateException() {
        assertTrue(IllegalStateException.class.isAssignableFrom(InvalidOrderStateException.class));
    }

    @Test
    void duplicateOrderExceptionIsAlsoAValidationException() {
        assertTrue(ValidationException.class.isAssignableFrom(DuplicateOrderException.class));
    }
}