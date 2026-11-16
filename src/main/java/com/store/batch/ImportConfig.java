package com.store.batch;

import com.store.exception.ConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// Where to read the orders from and where to write the reports.
// Nothing is hardcoded: the values come from environment variables or a configuration file.
// An environment variable wins over the same setting in the file.
public record ImportConfig(Path inputFile, Path outputDir) {

    public static final String ENV_INPUT = "ORDER_IMPORT_INPUT";
    public static final String ENV_OUTPUT_DIR = "ORDER_IMPORT_OUTPUT_DIR";
    public static final String ENV_CONFIG_FILE = "ORDER_IMPORT_CONFIG";
    public static final String DEFAULT_CONFIG_FILE = "config/importer.properties";
    public static final String KEY_INPUT = "input.file";
    public static final String KEY_OUTPUT_DIR = "output.dir";

    // env is normally System.getenv(). Passing it in makes the method easy to test.
    public static ImportConfig load(Map<String, String> env, Path configFile) {
        Properties fileSettings = readFile(configFile);

        String input = firstNonBlank(env.get(ENV_INPUT), fileSettings.getProperty(KEY_INPUT));
        String outputDir = firstNonBlank(env.get(ENV_OUTPUT_DIR), fileSettings.getProperty(KEY_OUTPUT_DIR));

        List<String> missing = new ArrayList<>();
        if (input == null) {
            missing.add("the input file (environment variable " + ENV_INPUT
                    + " or " + KEY_INPUT + " in the configuration file)");
        }
        if (outputDir == null) {
            missing.add("the output folder (environment variable " + ENV_OUTPUT_DIR
                    + " or " + KEY_OUTPUT_DIR + " in the configuration file)");
        }
        if (!missing.isEmpty()) {
            throw new ConfigurationException("Missing setting: " + String.join(" and ", missing) + ".");
        }

        try {
            return new ImportConfig(Path.of(input), Path.of(outputDir));
        } catch (InvalidPathException e) {
            throw new ConfigurationException("A configured path is not valid: " + e.getInput());
        }
    }

    private static Properties readFile(Path configFile) {
        Properties settings = new Properties();
        if (configFile == null || !Files.exists(configFile)) {
            return settings; // no file is fine when environment variables are used
        }
        try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            settings.load(reader);
        } catch (IOException e) {
            throw new ConfigurationException("Could not read the configuration file "
                    + configFile.getFileName() + ".");
        }
        return settings;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }
}