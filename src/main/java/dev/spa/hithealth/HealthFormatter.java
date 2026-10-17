package dev.spa.hithealth;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;

/** Creates action-bar text from a target's name, health and damage dealt. */
public final class HealthFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String DEFAULT_FORMAT = "<white><name></white> <color>❤ <hp>/<max></color>";
    private static final String DEFAULT_DAMAGE_FORMAT = " <gray>(-<damage>)</gray>";
    private static final double DEFAULT_HIGH = 0.5;
    private static final double DEFAULT_LOW = 0.25;

    private final String format;
    private final boolean damageEnabled;
    private final String damageFormat;
    private final double high;
    private final double low;
    private final TextColor highColor;
    private final TextColor midColor;
    private final TextColor lowColor;
    private final int decimals;

    public HealthFormatter(ConfigurationSection config) {
        this(config, null);
    }

    /** Validates configuration once, so invalid settings do not spam warnings on each hit. */
    public HealthFormatter(ConfigurationSection config, Logger logger) {
        Object configuredHigh = config.get("thresholds.high", DEFAULT_HIGH);
        Object configuredLow = config.get("thresholds.low", DEFAULT_LOW);
        if (validThreshold(configuredHigh) && validThreshold(configuredLow)
                && ((Number) configuredHigh).doubleValue() >= ((Number) configuredLow).doubleValue()) {
            high = ((Number) configuredHigh).doubleValue();
            low = ((Number) configuredLow).doubleValue();
        } else {
            warn(logger, "Thresholds must be finite numbers from 0 to 1 with high >= low; using 0.5 and 0.25.");
            high = DEFAULT_HIGH;
            low = DEFAULT_LOW;
        }
        highColor = color(config, "colors.high", TextColor.color(0x55FF55), logger);
        midColor = color(config, "colors.mid", TextColor.color(0xFFFF55), logger);
        lowColor = color(config, "colors.low", TextColor.color(0xFF5555), logger);
        decimals = decimals(config.get("decimals", 1), logger);

        Object configuredDamageEnabled = config.get("damage.enabled", false);
        if (configuredDamageEnabled instanceof Boolean value) {
            damageEnabled = value;
        } else {
            warn(logger, "'damage.enabled' must be true or false; using false.");
            damageEnabled = false;
        }
        format = validatedFormat(config, "format", DEFAULT_FORMAT, logger);
        damageFormat = validatedFormat(config, "damage.format", DEFAULT_DAMAGE_FORMAT, logger);
    }

    public Component render(Component name, double health, double maxHealth) {
        return render(name, health, maxHealth, 0);
    }

    /** Damage is the final, health-capped damage of the hit, supplied by the caller. */
    public Component render(Component name, double health, double maxHealth, double damage) {
        String displayFormat = damageEnabled ? format + damageFormat : format;
        return MINI_MESSAGE.deserialize(displayFormat, placeholders(name, health, maxHealth, damage));
    }

    public TextColor colorFor(double health, double maxHealth) {
        double maximum = nonNegativeFinite(maxHealth);
        double ratio = maximum > 0 ? nonNegativeFinite(health) / maximum : 0;
        return ratio >= high ? highColor : ratio >= low ? midColor : lowColor;
    }

    private TagResolver placeholders(Component name, double health, double maxHealth, double damage) {
        return TagResolver.resolver(
                Placeholder.component("name", name),
                Placeholder.unparsed("hp", number(health)),
                Placeholder.unparsed("max", number(maxHealth)),
                Placeholder.unparsed("damage", number(damage)),
                TagResolver.resolver("color", Tag.styling(colorFor(health, maxHealth))));
    }

    private String validatedFormat(ConfigurationSection config, String path, String fallback, Logger logger) {
        Object configured = config.get(path, fallback);
        if (!(configured instanceof String value)) {
            warn(logger, "'" + path + "' must be a MiniMessage string; using the default.");
            return fallback;
        }
        try {
            // Keep MiniMessage's normal leniency, including unclosed formatting tags.
            MINI_MESSAGE.deserialize(value, placeholders(Component.empty(), 0, 1, 0));
            return value;
        } catch (RuntimeException exception) {
            warn(logger, "Invalid MiniMessage in '" + path + "'; using the default.");
            return fallback;
        }
    }

    private String number(double value) {
        // Round upward so that a living target with fractional HP is never shown as zero.
        return BigDecimal.valueOf(nonNegativeFinite(value)).setScale(decimals, RoundingMode.CEILING).toPlainString();
    }

    private static double nonNegativeFinite(double value) {
        return Double.isFinite(value) ? Math.max(0, value) : 0;
    }

    private static boolean validThreshold(Object value) {
        if (!(value instanceof Number number)) {
            return false;
        }
        double threshold = number.doubleValue();
        return Double.isFinite(threshold) && threshold >= 0 && threshold <= 1;
    }

    private static int decimals(Object value, Logger logger) {
        if (value instanceof Number number) {
            double configured = number.doubleValue();
            if (Double.isFinite(configured) && configured == Math.rint(configured)) {
                if (configured < 0 || configured > 2) {
                    warn(logger, "'decimals' must be from 0 to 2; clamping to the nearest supported value.");
                }
                return (int) Math.max(0, Math.min(2, configured));
            }
        }
        warn(logger, "'decimals' must be an integer from 0 to 2; using 1.");
        return 1;
    }

    private static TextColor color(ConfigurationSection config, String path, TextColor fallback, Logger logger) {
        Object value = config.get(path);
        TextColor parsed = value instanceof String text ? TextColor.fromHexString(text) : null;
        if (value != null && parsed == null) {
            warn(logger, "'" + path + "' must be a hex color; using " + fallback.asHexString() + ".");
        }
        return parsed == null ? fallback : parsed;
    }

    private static void warn(Logger logger, String message) {
        if (logger != null) {
            logger.warning(message);
        }
    }
}
