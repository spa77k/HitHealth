package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerPreferencesTest {

    private static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @TempDir
    Path directory;

    private final Logger logger = Logger.getAnonymousLogger();

    @Test
    void newPlayersAreEnabledWithoutCreatingAFile() {
        Path file = directory.resolve("players.yml");
        PlayerPreferences preferences = new PlayerPreferences(file.toFile(), logger);

        assertTrue(preferences.isEnabledFor(FIRST));
        preferences.setEnabledFor(FIRST, true);

        assertFalse(Files.exists(file));
    }

    @Test
    void disabledPlayersSurviveReloadAndAreSavedInSortedOrder() {
        Path file = directory.resolve("players.yml");
        PlayerPreferences preferences = new PlayerPreferences(file.toFile(), logger);

        preferences.setEnabledFor(SECOND, false);
        preferences.setEnabledFor(FIRST, false);

        assertEquals(List.of(FIRST.toString(), SECOND.toString()),
                YamlConfiguration.loadConfiguration(file.toFile()).getStringList("disabled"));
        PlayerPreferences reloaded = new PlayerPreferences(file.toFile(), logger);
        assertFalse(reloaded.isEnabledFor(FIRST));
        assertFalse(reloaded.isEnabledFor(SECOND));
    }

    @Test
    void enablingAPlayerRemovesOnlyTheirDisabledPreference() {
        Path file = directory.resolve("players.yml");
        PlayerPreferences preferences = new PlayerPreferences(file.toFile(), logger);
        preferences.setEnabledFor(FIRST, false);
        preferences.setEnabledFor(SECOND, false);

        preferences.setEnabledFor(FIRST, true);

        PlayerPreferences reloaded = new PlayerPreferences(file.toFile(), logger);
        assertTrue(reloaded.isEnabledFor(FIRST));
        assertFalse(reloaded.isEnabledFor(SECOND));
        assertEquals(List.of(SECOND.toString()),
                YamlConfiguration.loadConfiguration(file.toFile()).getStringList("disabled"));
    }

    @Test
    void enablingTheLastDisabledPlayerPersistsAnEmptyList() {
        Path file = directory.resolve("players.yml");
        PlayerPreferences preferences = new PlayerPreferences(file.toFile(), logger);
        preferences.setEnabledFor(FIRST, false);

        preferences.setEnabledFor(FIRST, true);

        assertTrue(new PlayerPreferences(file.toFile(), logger).isEnabledFor(FIRST));
        assertEquals(List.of(), YamlConfiguration.loadConfiguration(file.toFile()).getStringList("disabled"));
    }

    @Test
    void unchangedPreferencesDoNotRewriteTheFile() throws IOException {
        Path file = directory.resolve("players.yml");
        String original = "# Preserve this file unless a preference changes\ndisabled:\n  - " + FIRST + "\n";
        Files.writeString(file, original);
        PlayerPreferences preferences = new PlayerPreferences(file.toFile(), logger);

        preferences.setEnabledFor(FIRST, false);
        preferences.setEnabledFor(SECOND, true);

        assertEquals(original, Files.readString(file));
    }

    @Test
    void invalidUuidsAreLoggedWithoutDiscardingValidPreferences() throws IOException {
        Path file = directory.resolve("players.yml");
        Files.writeString(file, "disabled:\n  - " + FIRST + "\n  - invalid-uuid\n  - " + SECOND + "\n");
        List<LogRecord> logs = captureLogs();

        PlayerPreferences preferences = new PlayerPreferences(file.toFile(), logger);

        assertFalse(preferences.isEnabledFor(FIRST));
        assertFalse(preferences.isEnabledFor(SECOND));
        assertEquals(1, logs.size());
        assertEquals(Level.WARNING, logs.getFirst().getLevel());
        assertEquals("Ignored invalid UUID in players.yml: invalid-uuid", logs.getFirst().getMessage());
    }

    @Test
    void saveFailureIsLoggedAndKeepsTheInMemoryPreference() throws IOException {
        Path file = directory.resolve("players.yml");
        PlayerPreferences preferences = new PlayerPreferences(file.toFile(), logger);
        Files.createDirectory(file);
        List<LogRecord> logs = captureLogs();

        preferences.setEnabledFor(FIRST, false);

        assertFalse(preferences.isEnabledFor(FIRST));
        assertEquals(1, logs.size());
        assertEquals(Level.WARNING, logs.getFirst().getLevel());
        assertEquals("Could not save players.yml", logs.getFirst().getMessage());
        assertInstanceOf(IOException.class, logs.getFirst().getThrown());
    }

    private List<LogRecord> captureLogs() {
        List<LogRecord> logs = new ArrayList<>();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                logs.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        return logs;
    }
}
