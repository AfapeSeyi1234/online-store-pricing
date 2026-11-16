package com.store.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.exception.DataFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class OrderImportJobTest {

    private static final String HEADER =
            "orderId,customerId,productId,quantity,unitPrice,orderDate,createdAt,status";
    private static final String GOOD_1 = "O1,C1,P1,2,50.00,2026-10-01,2026-10-01T09:15:00,DELIVERED";
    private static final String GOOD_2 = "O2,C2,P2,10,2.50,2026-10-02,2026-10-02T10:30:00,PAID";
    private static final String BAD = "O3,C3,P3,1,120.00,2026-13-45,2026-10-03T14:05:00,SHIPPED";

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), ZoneOffset.UTC);

    @TempDir
    Path tempDir;

    private Path writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("orders.csv");
        Files.write(file, List.of(lines));
        return file;
    }

    private ImportConfig configFor(Path input) {
        return new ImportConfig(input, tempDir.resolve("out"));
    }

    private JsonNode readReport() throws IOException {
        String json = Files.readString(tempDir.resolve("out").resolve(OrderImportJob.REPORT_FILE));
        return new ObjectMapper().readTree(json);
    }

    // ---------- valid files ----------

    @Test
    void validFileCreatesTheJsonReportAndTheRejectedReport() throws Exception {
        OrderImportJob.run(configFor(writeCsv(HEADER, GOOD_1, GOOD_2, BAD)), clock);
        assertTrue(Files.exists(tempDir.resolve("out").resolve(OrderImportJob.REPORT_FILE)));
        assertTrue(Files.exists(tempDir.resolve("out").resolve(OrderImportJob.REJECTED_FILE)));
    }

    @Test
    void jsonReportContainsRecordsTotalsAndTheProcessingTime() throws Exception {
        OrderImportJob.run(configFor(writeCsv(HEADER, GOOD_1, GOOD_2, BAD)), clock);
        JsonNode report = readReport();

        assertEquals("2026-11-02T10:00:00", report.get("processedAt").asText());
        assertEquals(2, report.get("processedRecords").size());
        assertEquals(1, report.get("rejectedRecords").size());
        assertEquals(3, report.get("summary").get("recordsRead").asInt());
        assertEquals(2, report.get("summary").get("acceptedCount").asInt());
        assertEquals(1, report.get("summary").get("rejectedCount").asInt());
        assertEquals(125.00, report.get("summary").get("totalAmount").asDouble(), 0.001);
        assertEquals("O1", report.get("processedRecords").get(0).get("orderId").asText());
        assertEquals("2026-10-01", report.get("processedRecords").get(0).get("orderDate").asText());
    }

    @Test
    void jsonReportListsTheRejectionReasons() throws Exception {
        OrderImportJob.run(configFor(writeCsv(HEADER, GOOD_1, BAD)), clock);
        JsonNode rejected = readReport().get("rejectedRecords").get(0);
        assertEquals(3, rejected.get("lineNumber").asInt());
        assertTrue(rejected.get("reasons").get(0).asText().contains("orderDate"));
    }

    @Test
    void rejectedReportFileShowsTheLineNumberAndTheReason() throws Exception {
        OrderImportJob.run(configFor(writeCsv(HEADER, GOOD_1, GOOD_2, BAD)), clock);
        String text = Files.readString(tempDir.resolve("out").resolve(OrderImportJob.REJECTED_FILE));
        assertTrue(text.contains("Line 4"));
        assertTrue(text.contains("orderDate '2026-13-45'"));
    }

    @Test
    void outputFolderIsCreatedWhenItDoesNotExist() throws Exception {
        assertTrue(Files.notExists(tempDir.resolve("out")));
        OrderImportJob.run(configFor(writeCsv(HEADER, GOOD_1)), clock);
        assertTrue(Files.isDirectory(tempDir.resolve("out")));
    }

    // ---------- empty and duplicate ----------

    @Test
    void emptyFileGivesAnEmptyReportWithZeroTotals() throws Exception {
        Path empty = tempDir.resolve("orders.csv");
        Files.write(empty, new byte[0]);

        ImportResult result = OrderImportJob.run(configFor(empty), clock);

        assertEquals(0, result.recordsRead());
        JsonNode report = readReport();
        assertEquals(0, report.get("processedRecords").size());
        assertEquals(0.0, report.get("summary").get("totalAmount").asDouble(), 0.001);
        assertEquals(0.0, report.get("summary").get("averageOrderValue").asDouble(), 0.001);
    }

    @Test
    void duplicateRecordsInTheFileAreRejected() throws Exception {
        ImportResult result = OrderImportJob.run(configFor(writeCsv(HEADER, GOOD_1, GOOD_1)), clock);
        assertEquals(1, result.acceptedCount());
        assertEquals(1, result.rejectedCount());
    }

    // ---------- unavailable paths ----------

    @Test
    void missingInputFileGivesAClearError() {
        DataFileException e = assertThrows(DataFileException.class,
                () -> OrderImportJob.run(configFor(tempDir.resolve("missing.csv")), clock));
        assertEquals("Input file not found: missing.csv", e.getMessage());
    }

    @Test
    void inputPathThatIsAFolderGivesAClearError() {
        assertThrows(DataFileException.class, () -> OrderImportJob.run(configFor(tempDir), clock));
    }

    @Test
    void unavailableOutputFolderGivesAClearError() throws Exception {
        Path blocker = tempDir.resolve("blocker.txt");
        Files.writeString(blocker, "this is a file, not a folder");
        ImportConfig config = new ImportConfig(writeCsv(HEADER, GOOD_1), blocker.resolve("sub"));
        assertThrows(DataFileException.class, () -> OrderImportJob.run(config, clock));
    }

    // ---------- the real sample file ----------

    @Test
    void sampleFileHasAtLeastTwentyRecordsAndTheyAreSplitCorrectly() throws Exception {
        Path sample = Path.of("data", "sample-orders.csv");
        assertTrue(Files.exists(sample), "Run the tests from the project folder so data/sample-orders.csv is found.");

        ImportResult result = OrderImportJob.run(configFor(sample), clock);

        assertTrue(result.recordsRead() >= 20);
        assertEquals(21, result.acceptedCount());
        assertEquals(8, result.rejectedCount());
        assertEquals(2141.29, result.totalAmount(), 0.01);
    }
}