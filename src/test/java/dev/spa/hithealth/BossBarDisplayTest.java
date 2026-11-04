package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Logger;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class BossBarDisplayTest {

    private final YamlConfiguration config = new YamlConfiguration();

    @Test
    void progressUsesTheClampedHealthRatio() {
        assertEquals(0.0f, BossBarDisplay.progress(0, 20));
        assertEquals(0.5f, BossBarDisplay.progress(10, 20));
        assertEquals(1.0f, BossBarDisplay.progress(20, 20));
        assertEquals(1.0f, BossBarDisplay.progress(100, 20));
        assertEquals(0.0f, BossBarDisplay.progress(10, 0));
        assertEquals(0.0f, BossBarDisplay.progress(10, -1));
        assertEquals(0.0f, BossBarDisplay.progress(Double.NaN, 20));
        assertEquals(0.0f, BossBarDisplay.progress(Double.POSITIVE_INFINITY, 20));
        assertEquals(0.0f, BossBarDisplay.progress(10, Double.POSITIVE_INFINITY));
    }

    @Test
    void colorByHealthUsesTheSameThresholdsAsTheTextColor() {
        HealthFormatter formatter = settings().formatter();

        assertEquals(BossBar.Color.GREEN, formatter.bossBarColorFor(10, 20, true, BossBar.Color.RED));
        assertEquals(BossBar.Color.YELLOW, formatter.bossBarColorFor(5, 20, true, BossBar.Color.RED));
        assertEquals(BossBar.Color.RED, formatter.bossBarColorFor(4.99, 20, true, BossBar.Color.RED));
        assertEquals(BossBar.Color.RED, formatter.bossBarColorFor(10, 0, true, BossBar.Color.RED));
    }

    @Test
    void configuredColorIsUsedWhenColorByHealthIsDisabled() {
        config.set("bossbar.color-by-health", false);
        config.set("bossbar.color", "BLUE");
        HealthFormatter formatter = settings().formatter();

        assertEquals(BossBar.Color.BLUE, formatter.bossBarColorFor(20, 20, false, BossBar.Color.BLUE));
        assertEquals(BossBar.Color.BLUE, formatter.bossBarColorFor(0, 20, false, BossBar.Color.BLUE));
    }

    @Test
    void customThresholdsStillDriveBossBarColors() {
        config.set("thresholds.high", 0.8);
        config.set("thresholds.low", 0.4);
        HealthFormatter formatter = settings().formatter();

        assertEquals(BossBar.Color.GREEN, formatter.bossBarColorFor(16, 20, true, BossBar.Color.RED));
        assertEquals(BossBar.Color.YELLOW, formatter.bossBarColorFor(8, 20, true, BossBar.Color.RED));
        assertEquals(BossBar.Color.RED, formatter.bossBarColorFor(7.99, 20, true, BossBar.Color.RED));
    }

    private HitHealthSettings settings() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        return HitHealthSettings.load(config, logger);
    }
}
