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
import net.kyori.adventure.bossbar.BossBar;
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

    @Test
    void displayModeDefaultsToActionBarAndParsesCaseInsensitively() {
        assertEquals(DisplayMode.ACTIONBAR, load().displayMode());

        config.set("display.mode", "BoSsBaR");
        assertEquals(DisplayMode.BOSSBAR, load().displayMode());
        config.set("display.mode", "both");
        assertEquals(DisplayMode.BOTH, load().displayMode());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void invalidDisplayModeWarnsOnceAndFallsBackToActionBar() {
        config.set("display.mode", "sidebar");

        assertEquals(DisplayMode.ACTIONBAR, load().displayMode());
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("display.mode"));
    }

    @Test
    void bossBarSettingsHaveDefaultsAndAcceptValidValues() {
        BossBarSettings defaults = load().bossBarSettings();
        assertTrue(defaults.colorByHealth());
        assertEquals(BossBar.Color.RED, defaults.color());
        assertEquals(BossBar.Overlay.PROGRESS, defaults.overlay());
        assertEquals(60, defaults.hideAfterTicks());
        assertTrue(warnings.isEmpty());

        config.set("bossbar.color-by-health", false);
        config.set("bossbar.color", "pink");
        config.set("bossbar.overlay", "notched_10");
        config.set("bossbar.hide-after-seconds", 0.001);
        BossBarSettings custom = load().bossBarSettings();
        assertFalse(custom.colorByHealth());
        assertEquals(BossBar.Color.PINK, custom.color());
        assertEquals(BossBar.Overlay.NOTCHED_10, custom.overlay());
        assertEquals(1, custom.hideAfterTicks());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void invalidBossBarValuesWarnAndUseDefaults() {
        config.set("bossbar.color-by-health", "yes");
        config.set("bossbar.color", "RAINBOW");
        config.set("bossbar.overlay", "SOLID");
        config.set("bossbar.hide-after-seconds", 0);

        BossBarSettings settings = load().bossBarSettings();
        assertTrue(settings.colorByHealth());
        assertEquals(BossBar.Color.RED, settings.color());
        assertEquals(BossBar.Overlay.PROGRESS, settings.overlay());
        assertEquals(60, settings.hideAfterTicks());
        assertEquals(4, warnings.size());
        assertTrue(warnings.stream().allMatch(warning -> warning.contains("bossbar.")));
    }

    @Test
    void bossBarHideAfterSecondsIsValidated() {
        for (Object invalid : List.of(-1, 0.0, 60.1, Double.NaN, Double.POSITIVE_INFINITY, "3")) {
            warnings.clear();
            config.set("bossbar.hide-after-seconds", invalid);

            assertEquals(60, load().bossBarSettings().hideAfterTicks(), "hide after: " + invalid);
            assertTrue(warnings.stream().anyMatch(warning -> warning.contains("bossbar.hide-after-seconds")));
        }

        config.set("bossbar.hide-after-seconds", 60);
        assertEquals(1200, load().bossBarSettings().hideAfterTicks());
    }

    @Test
    void bossBarModeUsesItsOwnHideDelayWhenNoPositiveDisplayDurationIsSet() {
        assertEquals(0, load().effectiveDisplayDurationTicks());

        config.set("display.mode", "bossbar");
        assertEquals(60, load().effectiveDisplayDurationTicks());

        config.set("display.duration-seconds", 2);
        assertEquals(40, load().effectiveDisplayDurationTicks());

        config.set("display.mode", "both");
        assertEquals(40, load().effectiveDisplayDurationTicks());

        config.set("display.duration-seconds", 0);
        config.set("bossbar.hide-after-seconds", 1.5);
        assertEquals(30, load().effectiveDisplayDurationTicks());

        config.set("display.mode", "actionbar");
        assertEquals(0, load().effectiveDisplayDurationTicks());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void barSettingsHaveDefaultsAndAcceptValidValues() {
        BarSettings defaults = load().barSettings();
        assertEquals(10, defaults.length());
        assertEquals("■", defaults.full());
        assertEquals("□", defaults.empty());
        assertEquals(net.kyori.adventure.text.format.TextColor.color(0x555555), defaults.emptyColor());
        assertTrue(warnings.isEmpty());

        config.set("bar.length", 4);
        config.set("bar.full", "#");
        config.set("bar.empty", "");
        config.set("bar.empty-color", "#112233");
        BarSettings custom = load().barSettings();
        assertEquals(4, custom.length());
        assertEquals("#", custom.full());
        assertEquals("", custom.empty());
        assertEquals(net.kyori.adventure.text.format.TextColor.color(0x112233), custom.emptyColor());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void invalidBarSettingsWarnAndUseDefaults() {
        config.set("bar.length", 0);
        config.set("bar.full", "");
        config.set("bar.empty", 12);
        config.set("bar.empty-color", "not-a-color");

        BarSettings settings = load().barSettings();
        assertEquals(10, settings.length());
        assertEquals("■", settings.full());
        assertEquals("□", settings.empty());
        assertEquals(net.kyori.adventure.text.format.TextColor.color(0x555555), settings.emptyColor());
        assertEquals(4, warnings.size());
        assertTrue(warnings.stream().allMatch(warning -> warning.contains("bar.")));
    }

    @Test
    void placeholderTimeoutDefaultsToTenSecondsAndIsValidated() {
        assertEquals(10_000, load().placeholderTimeoutMillis());
        config.set("placeholderapi.target-timeout-seconds", 1);
        assertEquals(1_000, load().placeholderTimeoutMillis());
        config.set("placeholderapi.target-timeout-seconds", 600);
        assertEquals(600_000, load().placeholderTimeoutMillis());
        assertTrue(warnings.isEmpty());

        for (Object invalid : List.of(0, 601, 1.5, Double.NaN, "10")) {
            warnings.clear();
            config.set("placeholderapi.target-timeout-seconds", invalid);

            assertEquals(10_000, load().placeholderTimeoutMillis(), "timeout: " + invalid);
            assertTrue(warnings.stream().anyMatch(
                    warning -> warning.contains("placeholderapi.target-timeout-seconds")));
        }
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
