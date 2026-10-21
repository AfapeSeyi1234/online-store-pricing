package com.store.logging;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// One readable line per message, using the level names DEBUG, INFO, WARN and ERROR.
// For exceptions only the class name is written (never the message or stack trace),
// because exception messages can contain file paths or personal data.
public class LogFormatter extends Formatter {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        String time = LocalDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault()).format(TIME);
        StringBuilder line = new StringBuilder();
        line.append(time).append(' ')
                .append(String.format("%-5s", levelName(record.getLevel()))).append(' ')
                .append(shortName(record.getLoggerName())).append(" - ")
                .append(formatMessage(record)).append(System.lineSeparator());
        if (record.getThrown() != null) {
            line.append("    cause: ").append(record.getThrown().getClass().getSimpleName())
                    .append(System.lineSeparator());
        }
        return line.toString();
    }

    // java.util.logging uses FINE, WARNING and SEVERE. We show the more familiar names.
    static String levelName(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return "ERROR";
        }
        if (value >= Level.WARNING.intValue()) {
            return "WARN";
        }
        if (value >= Level.INFO.intValue()) {
            return "INFO";
        }
        return "DEBUG";
    }

    private static String shortName(String loggerName) {
        if (loggerName == null) {
            return "root";
        }
        int lastDot = loggerName.lastIndexOf('.');
        return lastDot < 0 ? loggerName : loggerName.substring(lastDot + 1);
    }
}