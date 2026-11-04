package dev.spa.hithealth;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

/** Validated, immutable configuration shared by all displays until the next reload. */
public final class HitHealthSettings {

    private static final double MAX_DURATION_SECONDS = 60.0;
    private static final double TICKS_PER_SECOND = 20.0;

    private static final int DEFAULT_PLACEHOLDER_TIMEOUT_SECONDS = 10;
    private static final int MIN_PLACEHOLDER_TIMEOUT_SECONDS = 1;
    private static final int MAX_PLACEHOLDER_TIMEOUT_SECONDS = 600;

    private final HealthFormatter formatter;
    private final boolean showPlayers;
    private final long displayDurationTicks;
    private final DisplayMode displayMode;
    private final BossBarSettings bossBarSettings;
    private final BarSettings barSettings;
    private final long placeholderTimeoutMillis;
    private final boolean exclusionsEnabled;
    private final Set<String> excludedWorlds;
    private final Set<EntityType> excludedEntityTypes;

    private HitHealthSettings(HealthFormatter formatter, boolean showPlayers, long displayDurationTicks,
            DisplayMode displayMode, BossBarSettings bossBarSettings, BarSettings barSettings,
            long placeholderTimeoutMillis, boolean exclusionsEnabled, Set<String> excludedWorlds,
            Set<EntityType> excludedEntityTypes) {
        this.formatter = formatter;
        this.showPlayers = showPlayers;
        this.displayDurationTicks = displayDurationTicks;
        this.displayMode = displayMode;
        this.bossBarSettings = bossBarSettings;
        this.barSettings = barSettings;
        this.placeholderTimeoutMillis = placeholderTimeoutMillis;
        this.exclusionsEnabled = exclusionsEnabled;
        this.excludedWorlds = Set.copyOf(excludedWorlds);
        this.excludedEntityTypes = Set.copyOf(excludedEntityTypes);
    }

    public static HitHealthSettings load(ConfigurationSection config, Logger logger) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(logger, "logger");

        Set<String> worlds = new HashSet<>();
        for (Object entry : list(config, "exclusions.worlds", logger)) {
            if (entry instanceof String world && !world.isBlank()) {
                worlds.add(normalizeWorld(world));
            } else {
                logger.warning("Invalid exclusions.worlds entry '" + entry + "'; ignoring it.");
            }
        }

        Set<EntityType> entityTypes = new HashSet<>();
        for (Object entry : list(config, "exclusions.entity-types", logger)) {
            if (entry instanceof String type) {
                try {
                    entityTypes.add(EntityType.valueOf(type.trim().toUpperCase(Locale.ROOT)));
                    continue;
                } catch (IllegalArgumentException ignored) {
                    // Configuration uses Bukkit EntityType names, rather than display names.
                }
            }
            logger.warning("Invalid exclusions.entity-types entry '" + entry
                    + "'; use a Bukkit EntityType name. Ignoring it.");
        }

        BarSettings barSettings = BarSettings.load(config, logger);
        return new HitHealthSettings(new HealthFormatter(config, logger, barSettings),
                bool(config, "show-players", true, logger), durationTicks(config, logger),
                displayMode(config, logger), BossBarSettings.load(config, logger), barSettings,
                placeholderTimeoutMillis(config, logger),
                bool(config, "exclusions.enabled", true, logger), worlds, entityTypes);
    }

    public HealthFormatter formatter() {
        return formatter;
    }

    public DisplayMode displayMode() {
        return displayMode;
    }

    public BossBarSettings bossBarSettings() {
        return bossBarSettings;
    }

    public BarSettings barSettings() {
        return barSettings;
    }

    public long placeholderTimeoutMillis() {
        return placeholderTimeoutMillis;
    }

    public boolean showPlayers() {
        return showPlayers;
    }

    /** Zero preserves the single-send display; positive values are rounded up to whole ticks. */
    public long displayDurationTicks() {
        return displayDurationTicks;
    }

    /**
     * The duration handed to the display controller. Boss bars cannot fade by themselves, so when
     * the mode includes a boss bar and no positive display duration is configured, the boss bar's
     * own hide delay is used instead.
     */
    public long effectiveDisplayDurationTicks() {
        return displayDurationTicks > 0 || !displayMode.showsBossBar()
                ? displayDurationTicks : bossBarSettings.hideAfterTicks();
    }

    /** A target is excluded if either its world or its entity type matches an enabled filter. */
    public boolean isExcluded(String worldName, EntityType entityType) {
        return exclusionsEnabled && ((worldName != null && excludedWorlds.contains(normalizeWorld(worldName)))
                || (entityType != null && excludedEntityTypes.contains(entityType)));
    }

    private static long durationTicks(ConfigurationSection config, Logger logger) {
        Object configured = config.get("display.duration-seconds");
        if (configured == null) {
            return 0;
        }
        if (!(configured instanceof Number number) || !Double.isFinite(number.doubleValue())
                || number.doubleValue() < 0) {
            logger.warning("Invalid display.duration-seconds '" + configured
                    + "'; expected a finite number from 0 to 60. Using 0 (single send).");
            return 0;
        }
        double seconds = number.doubleValue();
        if (seconds > MAX_DURATION_SECONDS) {
            logger.warning("display.duration-seconds exceeds 60; limiting it to 60 seconds.");
            seconds = MAX_DURATION_SECONDS;
        }
        return (long) Math.ceil(seconds * TICKS_PER_SECOND);
    }

    private static DisplayMode displayMode(ConfigurationSection config, Logger logger) {
        Object value = config.get("display.mode");
        if (value == null) {
            return DisplayMode.ACTIONBAR;
        }
        if (value instanceof String text) {
            try {
                return DisplayMode.valueOf(text.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // Fall through to the warning below.
            }
        }
        logger.warning("Invalid display.mode '" + value
                + "'; expected actionbar, bossbar, or both. Using actionbar.");
        return DisplayMode.ACTIONBAR;
    }

    private static long placeholderTimeoutMillis(ConfigurationSection config, Logger logger) {
        Object value = config.get("placeholderapi.target-timeout-seconds");
        if (value == null) {
            return DEFAULT_PLACEHOLDER_TIMEOUT_SECONDS * 1000L;
        }
        if (value instanceof Number number && Double.isFinite(number.doubleValue())
                && number.doubleValue() == Math.rint(number.doubleValue())
                && number.doubleValue() >= MIN_PLACEHOLDER_TIMEOUT_SECONDS
                && number.doubleValue() <= MAX_PLACEHOLDER_TIMEOUT_SECONDS) {
            return (long) number.doubleValue() * 1000L;
        }
        logger.warning("Invalid placeholderapi.target-timeout-seconds '" + value
                + "'; expected an integer from 1 to 600. Using "
                + DEFAULT_PLACEHOLDER_TIMEOUT_SECONDS + ".");
        return DEFAULT_PLACEHOLDER_TIMEOUT_SECONDS * 1000L;
    }

    private static boolean bool(ConfigurationSection config, String path, boolean fallback, Logger logger) {
        Object value = config.get(path);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean result) {
            return result;
        }
        logger.warning("Invalid " + path + " '" + value + "'; expected true or false. Using " + fallback + ".");
        return fallback;
    }

    private static List<?> list(ConfigurationSection config, String path, Logger logger) {
        Object value = config.get(path);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> values) {
            return values;
        }
        logger.warning("Invalid " + path + "; expected a list. Using an empty list.");
        return List.of();
    }

    private static String normalizeWorld(String worldName) {
        return worldName.trim().toLowerCase(Locale.ROOT);
    }
}
