package com.store.logging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.Test;

public class LogFormatterTest {

    private final LogFormatter formatter = new LogFormatter();

    private LogRecord record(Level level, String message) {
        LogRecord record = new LogRecord(level, message);
        record.setLoggerName("com.store.service.OrderService");
        return record;
    }

    @Test
    void levelsAreShownWithFamiliarNames() {
        assertTrue(formatter.format(record(Level.FINE, "a")).contains("DEBUG"));
        assertTrue(formatter.format(record(Level.INFO, "a")).contains("INFO"));
        assertTrue(formatter.format(record(Level.WARNING, "a")).contains("WARN"));
        assertTrue(formatter.format(record(Level.SEVERE, "a")).contains("ERROR"));
    }

    @Test
    void loggerNameIsShortenedToTheClassName() {
        String text = formatter.format(record(Level.INFO, "hello"));
        assertTrue(text.contains("OrderService - hello"));
        assertFalse(text.contains("com.store"));
    }

    @Test
    void exceptionDetailsShowOnlyTheClassName() {
        LogRecord record = record(Level.SEVERE, "failed");
        record.setThrown(new RuntimeException("secret path C:\\Users\\Someone"));
        String text = formatter.format(record);
        assertTrue(text.contains("RuntimeException"));
        assertFalse(text.contains("secret"));
    }
}