package com.store.batch;

import com.store.exception.DataFileException;
import com.store.validation.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

// The whole workflow: read the CSV, validate it, write the reports.
// Logging rule from Week 5: file names only, never full paths or personal data.
public final class OrderImportJob {

    public static final String REPORT_FILE = "orders-report.json";
    public static final String REJECTED_FILE = "rejected-records.txt";

    private static final Logger LOGGER = Logger.getLogger(OrderImportJob.class.getName());

    private OrderImportJob() {
    }

    // The clock is passed in so tests can use a fixed date and time.
    public static ImportResult run(ImportConfig config, Clock clock) throws DataFileException {
        Validator.requireNotNull(config, "Configuration");
        Validator.requireNotNull(clock, "Clock");
        LOGGER.info("Import started for file " + config.inputFile().getFileName());

        List<String> lines = readLines(config.inputFile());
        ImportResult result = OrderCsvParser.parse(lines, LocalDate.now(clock));
        LocalDateTime processedAt = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);

        createOutputFolder(config.outputDir());
        JsonReportWriter.write(result, processedAt, config.outputDir().resolve(REPORT_FILE));
        RejectedReportWriter.write(result, processedAt, config.outputDir().resolve(REJECTED_FILE));

        logSummary(result);
        return result;
    }

    private static List<String> readLines(Path file) throws DataFileException {
        if (!Files.exists(file)) {
            LOGGER.warning("Input file not found: " + file.getFileName());
            throw new DataFileException("Input file not found: " + file.getFileName());
        }
        if (Files.isDirectory(file)) {
            throw new DataFileException("Input path is a folder, not a file: " + file.getFileName());
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read input file " + file.getFileName(), e);
            throw new DataFileException("Could not read input file " + file.getFileName()
                    + " (" + e.getClass().getSimpleName() + ").", e);
        }
        return lines;
    }

    private static void createOutputFolder(Path folder) throws DataFileException {
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create output folder " + folder.getFileName(), e);
            throw new DataFileException("Could not create the output folder " + folder.getFileName()
                    + " (" + e.getClass().getSimpleName() + ").", e);
        }
    }

    private static void logSummary(ImportResult result) {
        LOGGER.info(String.format(Locale.US, "Import finished: %d accepted, %d rejected, total %.2f",
                result.acceptedCount(), result.rejectedCount(), result.totalAmount()));
        if (result.recordsRead() == 0) {
            LOGGER.warning("The input file contained no records.");
        }
        if (result.rejectedCount() > 0) {
            LOGGER.warning(result.rejectedCount() + " record(s) were rejected, see " + REJECTED_FILE);
        }
    }
}