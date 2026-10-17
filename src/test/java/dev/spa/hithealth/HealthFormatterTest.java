package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class HealthFormatterTest {

    private final YamlConfiguration config = new YamlConfiguration();

    @Test
    void defaultFormatShowsNameHealthAndMaximum() {
        assertEquals("Zombie ❤ 12.0/20.0", render(12, 20));
    }

    @Test
    void livingHealthIsRoundedUpWhileZeroAndNegativeHealthDisplayZero() {
        assertEquals("Zombie ❤ 0.1/20.0", render(0.03, 20));
        assertEquals("Zombie ❤ 0.0/20.0", render(0, 20));
        assertEquals("Zombie ❤ 0.0/20.0", render(-1, 20));
    }

    @Test
    void decimalPlacesAreClampedToZeroThroughTwo() {
        config.set("decimals", -1);
        assertEquals("Zombie ❤ 1/21", render(0.01, 20.01));

        config.set("decimals", 3);
        assertEquals("Zombie ❤ 0.01/20.01", render(0.001, 20.001));
    }

    @Test
    void defaultColorThresholdsAreInclusive() {
        HealthFormatter formatter = new HealthFormatter(config);

        assertEquals(TextColor.color(0x55FF55), formatter.colorFor(10, 20));
        assertEquals(TextColor.color(0xFFFF55), formatter.colorFor(9.99, 20));
        assertEquals(TextColor.color(0xFFFF55), formatter.colorFor(5, 20));
        assertEquals(TextColor.color(0xFF5555), formatter.colorFor(4.99, 20));
    }

    @Test
    void nonPositiveMaximumOrNegativeHealthUsesTheLowColor() {
        HealthFormatter formatter = new HealthFormatter(config);

        assertEquals(TextColor.color(0xFF5555), formatter.colorFor(10, 0));
        assertEquals(TextColor.color(0xFF5555), formatter.colorFor(10, -1));
        assertEquals(TextColor.color(0xFF5555), formatter.colorFor(-1, 20));
    }

    @Test
    void customThresholdsAndColorsAreUsed() {
        config.set("thresholds.high", 0.8);
        config.set("thresholds.low", 0.4);
        config.set("colors.high", "#123456");
        config.set("colors.mid", "#654321");
        config.set("colors.low", "#ABCDEF");
        HealthFormatter formatter = new HealthFormatter(config);

        assertEquals(TextColor.color(0x123456), formatter.colorFor(16, 20));
        assertEquals(TextColor.color(0x654321), formatter.colorFor(8, 20));
        assertEquals(TextColor.color(0xABCDEF), formatter.colorFor(7.99, 20));
    }

    @Test
    void invalidConfiguredColorsFallBackToDefaults() {
        config.set("colors.high", "not-a-color");
        config.set("colors.mid", "");
        config.set("colors.low", "invalid");
        HealthFormatter formatter = new HealthFormatter(config);

        assertEquals(TextColor.color(0x55FF55), formatter.colorFor(20, 20));
        assertEquals(TextColor.color(0xFFFF55), formatter.colorFor(8, 20));
        assertEquals(TextColor.color(0xFF5555), formatter.colorFor(1, 20));
    }

    @Test
    void customFormatResolvesTheHealthColor() {
        config.set("format", "<color><hp></color>");
        Component actual = new HealthFormatter(config).render(Component.text("Zombie"), 4, 20);

        assertEquals(Component.text("4.0", TextColor.color(0xFF5555)), actual);
    }

    @Test
    void namesAreInsertedAsComponentsWithoutParsingTheirTextAsTags() {
        config.set("format", "<name>");
        HealthFormatter formatter = new HealthFormatter(config);
        Component name = Component.text("<red>Boss</red>", TextColor.color(0x123456));

        assertEquals(name, formatter.render(name, 12, 20));
    }

    @Test
    void translatedNamesAreKeptForTheClientsLocale() {
        config.set("format", "<name>");
        Component name = Component.translatable("entity.minecraft.zombie");

        assertEquals(name, new HealthFormatter(config).render(name, 12, 20));
    }

    @Test
    void damageSuffixIsDisabledByDefault() {
        assertEquals("Zombie ❤ 12.0/20.0", render(12, 20, 8));
    }

    @Test
    void damageSuffixCanBeEnabled() {
        config.set("damage.enabled", true);

        assertEquals("Zombie ❤ 12.0/20.0 (-8.0)", render(12, 20, 8));
        assertEquals("Zombie ❤ 0.0/20.0 (-20.0)", render(0, 20, 20));
    }

    @Test
    void damageUsesTheConfiguredDecimalPlacesAndRoundsUp() {
        config.set("damage.enabled", true);
        config.set("decimals", 2);
        assertEquals("Zombie ❤ 12.00/20.00 (-0.01)", render(12, 20, 0.001));

        config.set("decimals", 0);
        assertEquals("Zombie ❤ 12/20 (-1)", render(12, 20, 0.001));
    }

    @Test
    void customDamageSuffixResolvesAllPlaceholders() {
        config.set("damage.enabled", true);
        config.set("damage.format", " <color><name> lost <damage>; <hp>/<max> left</color>");

        assertEquals("Zombie ❤ 12.0/20.0 Zombie lost 8.0; 12.0/20.0 left", render(12, 20, 8));
    }

    @Test
    void damageSuffixCanResetUnclosedFormattingInTheMainFormat() {
        config.set("format", "<red><name>");
        config.set("damage.enabled", true);
        config.set("damage.format", "<reset><damage>");
        Component expected = Component.empty()
                .append(Component.text("Zombie", NamedTextColor.RED))
                .append(Component.text("8.0"));
        Component actual = new HealthFormatter(config).render(Component.text("Zombie"), 12, 20, 8);

        assertEquals(expected.compact(), actual.compact());
    }

    @Test
    void damagePlaceholderCanBeUsedInTheMainFormatWithoutTheSuffix() {
        config.set("format", "<name>: <damage> damage, <hp>/<max>");

        assertEquals("Zombie: 8.0 damage, 12.0/20.0", render(12, 20, 8));
    }

    @Test
    void originalRenderOverloadUsesZeroDamage() {
        config.set("damage.enabled", true);

        assertEquals("Zombie ❤ 12.0/20.0 (-0.0)", render(12, 20));
    }

    @Test
    void customNameCannotInjectTagsIntoTheDamageSuffix() {
        config.set("format", "");
        config.set("damage.enabled", true);
        config.set("damage.format", "<name>");
        Component name = Component.text("<red>Boss</red>", TextColor.color(0x123456));
        Component actual = new HealthFormatter(config).render(name, 12, 20, 8);

        assertEquals("<red>Boss</red>", plain(actual));
        assertEquals(name, actual.compact());
    }

    @Test
    void emptyDamageSuffixIsAllowed() {
        config.set("damage.enabled", true);
        config.set("damage.format", "");

        assertEquals("Zombie ❤ 12.0/20.0", render(12, 20, 8));
    }

    @Test
    void nonFiniteNumbersAndNegativeDamageAreRenderedAsZero() {
        config.set("damage.enabled", true);

        assertEquals("Zombie ❤ 0.0/0.0 (-0.0)", render(Double.NaN, Double.POSITIVE_INFINITY, Double.NaN));
        assertEquals("Zombie ❤ 12.0/20.0 (-0.0)", render(12, 20, -1));
        assertEquals("Zombie ❤ 12.0/20.0 (-0.0)", render(12, 20, Double.POSITIVE_INFINITY));
    }

    @Test
    void invalidThresholdsFallBackToTheDefaultPair() {
        Object[][] invalidThresholds = {
                {Double.NaN, 0.25},
                {0.5, Double.POSITIVE_INFINITY},
                {1.1, 0.25},
                {0.5, -0.1},
                {0.2, 0.8},
                {"invalid", 0.25}
        };
        for (Object[] thresholds : invalidThresholds) {
            config.set("thresholds.high", thresholds[0]);
            config.set("thresholds.low", thresholds[1]);
            HealthFormatter formatter = new HealthFormatter(config);

            assertEquals(TextColor.color(0x55FF55), formatter.colorFor(10, 20));
            assertEquals(TextColor.color(0xFFFF55), formatter.colorFor(5, 20));
            assertEquals(TextColor.color(0xFF5555), formatter.colorFor(4.99, 20));
        }
    }

    @Test
    void equalThresholdsAndBoundaryValuesAreAllowed() {
        config.set("thresholds.high", 1);
        config.set("thresholds.low", 0);
        HealthFormatter formatter = new HealthFormatter(config);
        assertEquals(TextColor.color(0x55FF55), formatter.colorFor(20, 20));
        assertEquals(TextColor.color(0xFFFF55), formatter.colorFor(0, 20));

        config.set("thresholds.high", 0.5);
        config.set("thresholds.low", 0.5);
        formatter = new HealthFormatter(config);
        assertEquals(TextColor.color(0x55FF55), formatter.colorFor(10, 20));
        assertEquals(TextColor.color(0xFF5555), formatter.colorFor(9, 20));
    }

    @Test
    void invalidDecimalValuesUseTheDefault() {
        for (Object value : new Object[] {"invalid", 1.5, Double.NaN, Double.POSITIVE_INFINITY}) {
            config.set("decimals", value);
            assertEquals("Zombie ❤ 0.1/20.0", render(0.01, 20));
        }
    }

    @Test
    void invalidDamageEnabledValueUsesTheDefault() {
        config.set("damage.enabled", "yes");

        assertEquals("Zombie ❤ 12.0/20.0", render(12, 20, 8));
    }

    @Test
    void nonStringFormatsFallBackToDefaults() {
        config.set("format", List.of("invalid"));
        config.set("damage.enabled", true);
        config.set("damage.format", 123);

        assertEquals("Zombie ❤ 12.0/20.0 (-8.0)", render(12, 20, 8));
    }

    @Test
    void rejectedMiniMessageFormatsFallBackAndWarnOnlyOnConstruction() {
        config.set("format", "§c<name>");
        config.set("damage.enabled", true);
        config.set("damage.format", "§c<damage>");
        List<String> warnings = new ArrayList<>();
        HealthFormatter formatter = new HealthFormatter(config, logger(warnings));

        assertEquals(2, warnings.size());
        assertTrue(warnings.get(0).contains("'format'"));
        assertTrue(warnings.get(1).contains("'damage.format'"));
        for (int i = 0; i < 3; i++) {
            assertEquals("Zombie ❤ 12.0/20.0 (-8.0)",
                    plain(formatter.render(Component.text("Zombie"), 12, 20, 8)));
        }
        assertEquals(2, warnings.size());
    }

    @Test
    void lenientMiniMessageFormattingRemainsSupported() {
        config.set("format", "<red><name> <unknown>");
        config.set("damage.enabled", true);
        config.set("damage.format", " <gray><damage>");
        List<String> warnings = new ArrayList<>();
        HealthFormatter formatter = new HealthFormatter(config, logger(warnings));

        assertEquals("Zombie <unknown> 8.0", plain(formatter.render(Component.text("Zombie"), 12, 20, 8)));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void invalidSettingsWarnOnlyWhenTheFormatterIsBuilt() {
        config.set("thresholds.high", Double.NaN);
        config.set("colors.high", "invalid");
        config.set("decimals", "invalid");
        config.set("damage.enabled", "invalid");
        List<String> warnings = new ArrayList<>();
        HealthFormatter formatter = new HealthFormatter(config, logger(warnings));

        assertEquals(4, warnings.size());
        formatter.render(Component.text("Zombie"), 12, 20, 8);
        formatter.render(Component.text("Zombie"), 4, 20, 8);
        assertEquals(4, warnings.size());
    }

    private static Logger logger(List<String> warnings) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
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
        return logger;
    }

    private String render(double health, double maxHealth, double damage) {
        return plain(new HealthFormatter(config).render(Component.text("Zombie"), health, maxHealth, damage));
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private String render(double health, double maxHealth) {
        Component component = new HealthFormatter(config).render(Component.text("Zombie"), health, maxHealth);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
