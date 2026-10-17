package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

class HitHealthSettingsTest {

    private final YamlConfiguration config = new YamlConfiguration();
    private final List<String> warnings = new ArrayList<>();
    private final Logger logger = logger();

    @Test
    void defaultsKeepTheOriginalOneShotDisplayAndExcludeNothing() {
        HitHealthSettings settings = load();

        assertTrue(settings.showPlayers());
        assertEquals(0, settings.displayDurationTicks());
        assertFalse(settings.isExcluded("world", EntityType.ZOMBIE));
        assertFalse(settings.isExcluded("world", EntityType.PLAYER));
        assertNotNull(settings.formatter());
        assertSame(settings.formatter(), settings.formatter());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void customWorldsAndTypesMatchIndependentlyAndCaseInsensitively() {
        config.set("show-players", false);
        config.set("display.duration-seconds", 2.5);
        config.set("exclusions.worlds", List.of(" LOBBY ", "world_nether"));
        config.set("exclusions.entity-types", List.of("zombie", " Wither "));
        HitHealthSettings settings = load();

        assertFalse(settings.showPlayers());
        assertEquals(50, settings.displayDurationTicks());
        assertTrue(settings.isExcluded("lobby", EntityType.COW));
        assertTrue(settings.isExcluded("WORLD_NETHER", EntityType.COW));
        assertTrue(settings.isExcluded("world", EntityType.ZOMBIE));
        assertTrue(settings.isExcluded("world", EntityType.WITHER));
        assertFalse(settings.isExcluded("world", EntityType.COW));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void disablingExclusionsRetainsTheListsWithoutApplyingThem() {
        config.set("exclusions.enabled", false);
        config.set("exclusions.worlds", List.of("lobby"));
        config.set("exclusions.entity-types", List.of("ZOMBIE"));
        HitHealthSettings settings = load();

        assertFalse(settings.isExcluded("lobby", EntityType.COW));
        assertFalse(settings.isExcluded("world", EntityType.ZOMBIE));
        assertFalse(settings.isExcluded("lobby", EntityType.ZOMBIE));
    }

    @Test
    void settingsAreAnImmutableSnapshotOfTheConfiguration() {
        config.set("show-players", false);
        config.set("display.duration-seconds", 3);
        config.set("format", "<hp>");
        config.set("exclusions.worlds", new ArrayList<>(List.of("lobby")));
        config.set("exclusions.entity-types", new ArrayList<>(List.of("ZOMBIE")));
        HitHealthSettings settings = load();

        config.set("show-players", true);
        config.set("display.duration-seconds", 0);
        config.set("format", "changed");
        config.set("exclusions.enabled", false);
        config.set("exclusions.worlds", List.of("other"));
        config.set("exclusions.entity-types", List.of("COW"));

        assertFalse(settings.showPlayers());
        assertEquals(60, settings.displayDurationTicks());
        assertTrue(settings.isExcluded("lobby", EntityType.COW));
        assertTrue(settings.isExcluded("world", EntityType.ZOMBIE));
        assertFalse(settings.isExcluded("other", EntityType.COW));
        assertEquals("12.0", PlainTextComponentSerializer.plainText().serialize(
                settings.formatter().render(Component.text("Zombie"), 12, 20)));
    }

    @Test
    void invalidDurationsFallBackToSingleSendAndWarn() {
        for (Object invalid : List.of(-0.1, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, "2.5", "invalid", true)) {
            warnings.clear();
            config.set("display.duration-seconds", invalid);

            assertEquals(0, load().displayDurationTicks(), "duration: " + invalid);
            assertTrue(warnings.stream().anyMatch(warning -> warning.contains("display.duration-seconds")));
        }
    }

    @Test
    void positiveDurationsRoundUpToTicksAndClampAtSixtySeconds() {
        config.set("display.duration-seconds", 0.001);
        assertEquals(1, load().displayDurationTicks());
        config.set("display.duration-seconds", 1.01);
        assertEquals(21, load().displayDurationTicks());
        config.set("display.duration-seconds", 60);
        assertEquals(1200, load().displayDurationTicks());
        assertTrue(warnings.isEmpty());

        config.set("display.duration-seconds", 600);
        assertEquals(1200, load().displayDurationTicks());
        assertTrue(warnings.stream().anyMatch(warning -> warning.contains("limiting it to 60")));
    }

    @Test
    void invalidEntityNamesWarnAndAreIgnoredWithoutLosingValidEntries() {
        config.set("exclusions.entity-types", List.of("not_a_mob", "minecraft:zombie", "", 12, "COW"));
        HitHealthSettings settings = load();

        assertFalse(settings.isExcluded("world", EntityType.ZOMBIE));
        assertTrue(settings.isExcluded("world", EntityType.COW));
        assertEquals(4, warnings.size());
        assertTrue(warnings.stream().allMatch(warning -> warning.contains("exclusions.entity-types")));
    }

    @Test
    void malformedWorldsAndListsFallBackSafely() {
        config.set("exclusions.worlds", List.of("", "  ", 12, "world"));
        config.set("exclusions.entity-types", "ZOMBIE");
        HitHealthSettings settings = load();

        assertTrue(settings.isExcluded("WORLD", EntityType.COW));
        assertFalse(settings.isExcluded("other", EntityType.ZOMBIE));
        assertEquals(4, warnings.size());

        config.set("exclusions.worlds", "world");
        assertFalse(load().isExcluded("world", EntityType.COW));
    }

    @Test
    void malformedBooleansUseDefaultsAndWarn() {
        config.set("show-players", "no");
        config.set("exclusions.enabled", "yes");
        config.set("exclusions.worlds", List.of("lobby"));
        HitHealthSettings settings = load();

        assertTrue(settings.showPlayers());
        assertTrue(settings.isExcluded("lobby", EntityType.COW));
        assertEquals(2, warnings.size());
    }

    @Test
    void optionalNullFilterArgumentsDoNotMatch() {
        config.set("exclusions.worlds", List.of("lobby"));
        config.set("exclusions.entity-types", List.of("ZOMBIE"));
        HitHealthSettings settings = load();

        assertFalse(settings.isExcluded(null, null));
        assertTrue(settings.isExcluded(null, EntityType.ZOMBIE));
        assertTrue(settings.isExcluded("lobby", null));
    }

    private HitHealthSettings load() {
        return HitHealthSettings.load(config, logger);
    }

    private Logger logger() {
        Logger result = Logger.getAnonymousLogger();
        result.setUseParentHandlers(false);
        result.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        return result;
    }
}
