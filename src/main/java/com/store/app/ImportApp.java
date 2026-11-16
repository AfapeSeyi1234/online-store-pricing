package com.store.app;

import com.store.batch.ImportConfig;
import com.store.batch.ImportResult;
import com.store.batch.OrderImportJob;
import com.store.exception.ConfigurationException;
import com.store.exception.DataFileException;
import com.store.logging.LogSetup;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Locale;
import java.util.logging.Level;

// Reads its settings from environment variables or config/importer.properties.
public class ImportApp {

    public static void main(String[] args) {
        LogSetup.init(Level.INFO, "logs/store.log");
        try {
            ImportConfig config = ImportConfig.load(System.getenv(), configFilePath());
            ImportResult result = OrderImportJob.run(config, Clock.systemDefaultZone());
            printSummary(result, config);
        } catch (ConfigurationException e) {
            System.out.println("Configuration error: " + e.getMessage());
            System.exit(1);
        } catch (DataFileException e) {
            System.out.println("File error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Path configFilePath() {
        String name = System.getenv(ImportConfig.ENV_CONFIG_FILE);
        if (name == null || name.isBlank()) {
            name = ImportConfig.DEFAULT_CONFIG_FILE;
        }
        return Path.of(name.trim());
    }

    private static void printSummary(ImportResult result, ImportConfig config) {
        System.out.println("=== Order import finished ===");
        System.out.println("Records read:   " + result.recordsRead());
        System.out.println("Accepted:       " + result.acceptedCount());
        System.out.println("Rejected:       " + result.rejectedCount());
        System.out.println(String.format(Locale.US, "Total amount:   %.2f", result.totalAmount()));
        System.out.println(String.format(Locale.US, "Average order:  %.2f", result.averageOrderValue()));
        System.out.println("JSON report:    " + config.outputDir().resolve(OrderImportJob.REPORT_FILE));
        System.out.println("Rejected list:  " + config.outputDir().resolve(OrderImportJob.REJECTED_FILE));
    }
}