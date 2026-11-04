package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class BarSettingsTest {

    private static final TextColor FILLED = TextColor.color(0xFF5555);
    private static final TextColor EMPTY = TextColor.color(0x112233);

    private final YamlConfiguration config = new YamlConfiguration();

    @Test
    void fillCountHandlesZeroTinyAndFullHealth() {
        assertEquals(0, BarSettings.filledCount(0, 20, 10));
        assertEquals(0, BarSettings.filledCount(-1, 20, 10));
        assertEquals(1, BarSettings.filledCount(0.01, 20, 10));
        assertEquals(5, BarSettings.filledCount(10, 20, 10));
        assertEquals(10, BarSettings.filledCount(20, 20, 10));
        assertEquals(10, BarSettings.filledCount(100, 20, 10));
    }

    @Test
    void fillCountRespectsLengthBounds() {
        assertEquals(0, BarSettings.filledCount(0, 20, 1));
        assertEquals(1, BarSettings.filledCount(20, 20, 1));
        assertEquals(1, BarSettings.filledCount(0.01, 20, 1));
        assertEquals(40, BarSettings.filledCount(20, 20, 40));
        assertEquals(1, BarSettings.filledCount(20, 20, 0));
        assertEquals(0, BarSettings.filledCount(0, 20, 0));
        assertEquals(40, BarSettings.filledCount(20, 20, 100));
        assertEquals(0, BarSettings.filledCount(0, 20, 100));
    }

    @Test
    void configuredCharactersAndColorsAreRendered() {
        config.set("bar.length", 4);
        config.set("bar.full", "#");
        config.set("bar.empty", "-");
        config.set("bar.empty-color", "#112233");

        Component bar = BarSettings.load(config, null).render(10, 20, FILLED);

        assertEquals("##--", plain(bar));
        assertEquals(FILLED, bar.color());
        assertEquals(EMPTY, bar.children().get(0).color());
    }

    @Test
    void emptySegmentsMayBeEmpty() {
        config.set("bar.length", 3);
        config.set("bar.full", "#");
        config.set("bar.empty", "");

        BarSettings settings = BarSettings.load(config, null);

        assertEquals("##", plain(settings.render(20, 30, FILLED)));
        assertEquals("##", settings.plain(20, 30));
    }

    @Test
    void configuredCharactersCannotInjectMiniMessageTags() {
        config.set("bar.length", 2);
        config.set("bar.full", "<red>");
        config.set("bar.empty", "<blue>");
        config.set("bar.empty-color", "#112233");

        Component bar = BarSettings.load(config, null).render(10, 20, FILLED);

        assertEquals("<red><blue>", plain(bar));
        assertEquals(FILLED, bar.color());
        assertEquals(EMPTY, bar.children().get(0).color());
    }

    @Test
    void plainBarUsesTheSameFillCount() {
        config.set("bar.length", 4);
        config.set("bar.full", "F");
        config.set("bar.empty", "e");
        BarSettings settings = BarSettings.load(config, null);

        assertEquals("FFee", settings.plain(10, 20));
        assertEquals("Feee", settings.plain(0.01, 20));
        assertEquals("eeee", settings.plain(0, 20));
    }

    @Test
    void constructorClampsLengthAndKeepsDefaultsForNulls() {
        BarSettings settings = new BarSettings(0, null, null, null);

        assertEquals(1, settings.length());
        assertEquals(BarSettings.DEFAULT_FULL, settings.full());
        assertEquals(BarSettings.DEFAULT_EMPTY, settings.empty());
        assertEquals(BarSettings.DEFAULT_EMPTY_COLOR, settings.emptyColor());
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
