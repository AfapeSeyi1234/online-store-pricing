package com.store.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.store.exception.ConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ImportConfigTest {

    @TempDir
    Path tempDir;

    private Path writeConfig(String... lines) throws IOException {
        Path file = tempDir.resolve("importer.properties");
        Files.write(file, List.of(lines));
        return file;
    }

    @Test
    void settingsAreReadFromTheConfigFile() throws IOException {
        Path file = writeConfig("input.file=data/orders.csv", "output.dir=reports");
        ImportConfig config = ImportConfig.load(Map.of(), file);
        assertEquals(Path.of("data/orders.csv"), config.inputFile());
        assertEquals(Path.of("reports"), config.outputDir());
    }

    @Test
    void environmentVariablesWinOverTheConfigFile() throws IOException {
        Path file = writeConfig("input.file=from-file.csv", "output.dir=from-file-out");
        Map<String, String> env = Map.of(
                ImportConfig.ENV_INPUT, "from-env.csv",
                ImportConfig.ENV_OUTPUT_DIR, "from-env-out");
        ImportConfig config = ImportConfig.load(env, file);
        assertEquals(Path.of("from-env.csv"), config.inputFile());
        assertEquals(Path.of("from-env-out"), config.outputDir());
    }

    @Test
    void settingsCanComeFromEnvironmentVariablesAloneWithoutAnyFile() {
        Map<String, String> env = Map.of(
                ImportConfig.ENV_INPUT, "a.csv", ImportConfig.ENV_OUTPUT_DIR, "out");
        ImportConfig config = ImportConfig.load(env, tempDir.resolve("no-such-file.properties"));
        assertEquals(Path.of("a.csv"), config.inputFile());
    }

    @Test
    void aNullConfigFileIsAllowedWhenEnvironmentVariablesAreSet() {
        Map<String, String> env = Map.of(
                ImportConfig.ENV_INPUT, "a.csv", ImportConfig.ENV_OUTPUT_DIR, "out");
        assertEquals(Path.of("out"), ImportConfig.load(env, null).outputDir());
    }

    @Test
    void missingSettingsGiveAClearConfigurationError() {
        ConfigurationException e = assertThrows(ConfigurationException.class,
                () -> ImportConfig.load(Map.of(), tempDir.resolve("no-such-file.properties")));
        assertTrue(e.getMessage().contains(ImportConfig.ENV_INPUT));
        assertTrue(e.getMessage().contains(ImportConfig.ENV_OUTPUT_DIR));
    }

    @Test
    void onlyTheMissingSettingIsNamedInTheError() {
        Map<String, String> env = Map.of(ImportConfig.ENV_INPUT, "a.csv");
        ConfigurationException e = assertThrows(ConfigurationException.class,
                () -> ImportConfig.load(env, null));
        assertTrue(e.getMessage().contains(ImportConfig.ENV_OUTPUT_DIR));
        assertFalse(e.getMessage().contains(ImportConfig.ENV_INPUT));
    }

    @Test
    void aBlankEnvironmentValueCountsAsMissing() {
        Map<String, String> env = Map.of(
                ImportConfig.ENV_INPUT, "   ", ImportConfig.ENV_OUTPUT_DIR, "out");
        assertThrows(ConfigurationException.class, () -> ImportConfig.load(env, null));
    }
}