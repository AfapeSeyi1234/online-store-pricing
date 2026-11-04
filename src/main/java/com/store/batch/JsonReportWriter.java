package com.store.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.store.exception.DataFileException;
import com.store.model.OrderStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// Builds the JSON report with Jackson. Dates are written as text (2026-10-01).
public final class JsonReportWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonReportWriter() {
    }

    public static String toJson(ImportResult result, LocalDateTime processedAt) throws DataFileException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("processedAt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(processedAt));

        ObjectNode summary = root.putObject("summary");
        summary.put("recordsRead", result.recordsRead());
        summary.put("acceptedCount", result.acceptedCount());
        summary.put("rejectedCount", result.rejectedCount());
        summary.put("totalAmount", result.totalAmount());
        summary.put("averageOrderValue", result.averageOrderValue());
        ObjectNode byStatus = summary.putObject("totalsByStatus");
        for (Map.Entry<OrderStatus, Double> entry : result.totalsByStatus().entrySet()) {
            byStatus.put(entry.getKey().name(), entry.getValue());
        }

        ArrayNode processed = root.putArray("processedRecords");
        for (ImportedOrder order : result.accepted()) {
            ObjectNode node = processed.addObject();
            node.put("orderId", order.orderId());
            node.put("customerId", order.customerId());
            node.put("productId", order.productId());
            node.put("quantity", order.quantity());
            node.put("unitPrice", order.unitPrice());
            node.put("orderDate", DateTimeFormatter.ISO_LOCAL_DATE.format(order.orderDate()));
            node.put("createdAt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(order.createdAt()));
            node.put("status", order.status().name());
            node.put("lineTotal", order.lineTotal());
        }

        ArrayNode rejected = root.putArray("rejectedRecords");
        for (RejectedRecord record : result.rejected()) {
            ObjectNode node = rejected.addObject();
            node.put("lineNumber", record.lineNumber());
            node.put("rawLine", record.rawLine());
            ArrayNode reasons = node.putArray("reasons");
            for (String reason : record.reasons()) {
                reasons.add(reason);
            }
        }

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new DataFileException("Could not create the JSON report.", e);
        }
    }

    public static void write(ImportResult result, LocalDateTime processedAt, Path file)
            throws DataFileException {
        String json = toJson(result, processedAt);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(json);
            writer.newLine();
        } catch (IOException e) {
            throw new DataFileException("Could not write the report file " + file.getFileName()
                    + " (" + e.getClass().getSimpleName() + ").", e);
        }
    }
}