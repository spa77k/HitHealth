package dev.spa.hithealth;

import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;

/** Validated characters and colors for the {@code <bar>} placeholder. */
public final class BarSettings {

    public static final int DEFAULT_LENGTH = 10;
    public static final String DEFAULT_FULL = "■";
    public static final String DEFAULT_EMPTY = "□";
    public static final TextColor DEFAULT_EMPTY_COLOR = TextColor.color(0x555555);
    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 40;

    private final int length;
    private final String full;
    private final String empty;
    private final TextColor emptyColor;

    public BarSettings(int length, String full, String empty, TextColor emptyColor) {
        this.length = clampLength(length);
        this.full = full == null ? DEFAULT_FULL : full;
        this.empty = empty == null ? DEFAULT_EMPTY : empty;
        this.emptyColor = emptyColor == null ? DEFAULT_EMPTY_COLOR : emptyColor;
    }

    public static BarSettings load(ConfigurationSection config, Logger logger) {
        return new BarSettings(
                length(config, logger),
                full(config, logger),
                empty(config, logger),
                emptyColor(config, logger));
    }

    public int length() {
        return length;
    }

    public String full() {
        return full;
    }

    public String empty() {
        return empty;
    }

    public TextColor emptyColor() {
        return emptyColor;
    }

    /** Builds the bar as a component so configured characters cannot inject MiniMessage tags. */
    public Component render(double health, double maxHealth, TextColor filledColor) {
        int filled = filledCount(health, maxHealth, length);
        return Component.text(full.repeat(filled), filledColor)
                .append(Component.text(empty.repeat(length - filled), emptyColor));
    }

    /** Plain-text form for the PlaceholderAPI {@code target_bar} placeholder. */
    public String plain(double health, double maxHealth) {
        int filled = filledCount(health, maxHealth, length);
        return full.repeat(filled) + empty.repeat(length - filled);
    }

    /**
     * A living target always fills at least one segment. A target at or below zero HP fills none.
     */
    public static int filledCount(double health, double maxHealth, int length) {
        int boundedLength = clampLength(length);
        if (!Double.isFinite(health) || health <= 0) {
            return 0;
        }
        double ratio = HealthFormatter.healthRatio(health, maxHealth);
        return Math.max(1, Math.min(boundedLength, (int) Math.ceil(ratio * boundedLength)));
    }

    private static int clampLength(int length) {
        return Math.max(MIN_LENGTH, Math.min(MAX_LENGTH, length));
    }

    private static int length(ConfigurationSection config, Logger logger) {
        Object value = config.get("bar.length");
        if (value == null) {
            return DEFAULT_LENGTH;
        }
        if (value instanceof Number number && Double.isFinite(number.doubleValue())
                && number.doubleValue() == Math.rint(number.doubleValue())
                && number.doubleValue() >= MIN_LENGTH && number.doubleValue() <= MAX_LENGTH) {
            return (int) number.doubleValue();
        }
        warn(logger, "Invalid bar.length '" + value + "'; expected an integer from 1 to 40. Using "
                + DEFAULT_LENGTH + ".");
        return DEFAULT_LENGTH;
    }

    private static String full(ConfigurationSection config, Logger logger) {
        Object value = config.get("bar.full");
        if (value == null) {
            return DEFAULT_FULL;
        }
        if (value instanceof String text && !text.isEmpty()) {
            return text;
        }
        warn(logger, "Invalid bar.full '" + value + "'; expected a non-empty string. Using the default.");
        return DEFAULT_FULL;
    }

    private static String empty(ConfigurationSection config, Logger logger) {
        Object value = config.get("bar.empty");
        if (value == null) {
            return DEFAULT_EMPTY;
        }
        if (value instanceof String text) {
            return text;
        }
        warn(logger, "Invalid bar.empty '" + value + "'; expected a string. Using the default.");
        return DEFAULT_EMPTY;
    }

    private static TextColor emptyColor(ConfigurationSection config, Logger logger) {
        Object value = config.get("bar.empty-color");
        if (value == null) {
            return DEFAULT_EMPTY_COLOR;
        }
        TextColor parsed = value instanceof String text ? TextColor.fromHexString(text) : null;
        if (parsed == null) {
            warn(logger, "Invalid bar.empty-color '" + value + "'; expected a hex color. Using "
                    + DEFAULT_EMPTY_COLOR.asHexString() + ".");
            return DEFAULT_EMPTY_COLOR;
        }
        return parsed;
    }

    private static void warn(Logger logger, String message) {
        if (logger != null) {
            logger.warning(message);
        }
    }
}
