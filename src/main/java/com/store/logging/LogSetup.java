package com.store.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// Call LogSetup.init(...) once when the program starts.
public final class LogSetup {

    private static boolean done = false;

    private LogSetup() {
    }

    public static synchronized void init(Level level, String filePath) {
        if (done) {
            return;
        }
        Logger root = Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler); // remove Java's default handlers
        }
        root.setLevel(level);

        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(level);
        console.setFormatter(new LogFormatter());
        root.addHandler(console);

        try {
            Files.createDirectories(Path.of(filePath).toAbsolutePath().getParent());
            FileHandler file = new FileHandler(filePath, true); // true = add to the end of the file
            file.setLevel(level);
            file.setFormatter(new LogFormatter());
            root.addHandler(file);
        } catch (IOException e) {
            root.warning("Could not open the log file, logging to the console only.");
        }
        done = true;
    }
}