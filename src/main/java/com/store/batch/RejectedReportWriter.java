package com.store.batch;

import com.store.exception.DataFileException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// A short report a person can read: which lines were rejected, and why.
public final class RejectedReportWriter {

    private RejectedReportWriter() {
    }

    public static void write(ImportResult result, LocalDateTime processedAt, Path file)
            throws DataFileException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("Rejected records report");
            writer.newLine();
            writer.write("Processed at: " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(processedAt));
            writer.newLine();
            writer.write("Rejected " + result.rejectedCount() + " of " + result.recordsRead() + " records.");
            writer.newLine();

            for (RejectedRecord record : result.rejected()) {
                writer.newLine();
                writer.write("Line " + record.lineNumber());
                writer.newLine();
                for (String reason : record.reasons()) {
                    writer.write("  Reason: " + reason);
                    writer.newLine();
                }
                writer.write("  Record: " + record.rawLine());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new DataFileException("Could not write the rejected-records report "
                    + file.getFileName() + " (" + e.getClass().getSimpleName() + ").", e);
        }
    }
}