package dev.spa.hithealth;

import java.util.logging.Logger;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.ConfigurationSection;

/** Validated boss bar appearance and lifetime settings. */
public record BossBarSettings(boolean colorByHealth, BossBar.Color color, BossBar.Overlay overlay,
        long hideAfterTicks) {

    public static final boolean DEFAULT_COLOR_BY_HEALTH = true;
    public static final BossBar.Color DEFAULT_COLOR = BossBar.Color.RED;
    public static final BossBar.Overlay DEFAULT_OVERLAY = BossBar.Overlay.PROGRESS;
    public static final double DEFAULT_HIDE_AFTER_SECONDS = 3.0;

    private static final double MAX_HIDE_AFTER_SECONDS = 60.0;
    private static final double TICKS_PER_SECOND = 20.0;

    public static BossBarSettings load(ConfigurationSection config, Logger logger) {
        return new BossBarSettings(
                bool(config, "bossbar.color-by-health", DEFAULT_COLOR_BY_HEALTH, logger),
                color(config, "bossbar.color", DEFAULT_COLOR, logger),
                overlay(config, "bossbar.overlay", DEFAULT_OVERLAY, logger),
                hideAfterTicks(config, logger));
    }

    private static boolean bool(ConfigurationSection config, String path, boolean fallback, Logger logger) {
        Object value = config.get(path);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean result) {
            return result;
        }
        warn(logger, "Invalid " + path + " '" + value + "'; expected true or false. Using " + fallback + ".");
        return fallback;
    }

    private static BossBar.Color color(ConfigurationSection config, String path, BossBar.Color fallback,
            Logger logger) {
        Object value = config.get(path);
        if (value == null) {
            return fallback;
        }
        if (value instanceof String text) {
            try {
                return BossBar.Color.valueOf(text.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // Fall through to the warning below.
            }
        }
        warn(logger, "Invalid " + path + " '" + value + "'; expected a BossBar.Color name. Using "
                + fallback.name() + ".");
        return fallback;
    }

    private static BossBar.Overlay overlay(ConfigurationSection config, String path, BossBar.Overlay fallback,
            Logger logger) {
        Object value = config.get(path);
        if (value == null) {
            return fallback;
        }
        if (value instanceof String text) {
            try {
                return BossBar.Overlay.valueOf(text.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // Fall through to the warning below.
            }
        }
        warn(logger, "Invalid " + path + " '" + value + "'; expected a BossBar.Overlay name. Using "
                + fallback.name() + ".");
        return fallback;
    }

    private static long hideAfterTicks(ConfigurationSection config, Logger logger) {
        Object configured = config.get("bossbar.hide-after-seconds");
        if (configured == null) {
            return ticks(DEFAULT_HIDE_AFTER_SECONDS);
        }
        if (!(configured instanceof Number number) || !Double.isFinite(number.doubleValue())
                || number.doubleValue() <= 0 || number.doubleValue() > MAX_HIDE_AFTER_SECONDS) {
            warn(logger, "Invalid bossbar.hide-after-seconds '" + configured
                    + "'; expected a number greater than 0 and at most 60. Using "
                    + DEFAULT_HIDE_AFTER_SECONDS + ".");
            return ticks(DEFAULT_HIDE_AFTER_SECONDS);
        }
        return ticks(number.doubleValue());
    }

    private static long ticks(double seconds) {
        return (long) Math.ceil(seconds * TICKS_PER_SECOND);
    }

    private static void warn(Logger logger, String message) {
        if (logger != null) {
            logger.warning(message);
        }
    }
}
