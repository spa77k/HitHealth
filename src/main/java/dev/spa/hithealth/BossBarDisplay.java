package dev.spa.hithealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Owns one reusable boss bar per viewer and keeps it in sync with the latest hit. */
final class BossBarDisplay {

    private final Map<UUID, BossBar> bars = new HashMap<>();

    void show(Player viewer, HitHealthSettings settings, Component name, double health, double maxHealth) {
        UUID viewerId = viewer.getUniqueId();
        BossBarSettings bossSettings = settings.bossBarSettings();
        BossBar.Color color = settings.formatter().bossBarColorFor(health, maxHealth,
                bossSettings.colorByHealth(), bossSettings.color());
        BossBar bar = bars.get(viewerId);
        if (bar == null) {
            bar = BossBar.bossBar(name, progress(health, maxHealth), color, bossSettings.overlay());
            bars.put(viewerId, bar);
        } else {
            bar.name(name);
            bar.progress(progress(health, maxHealth));
            bar.color(color);
            bar.overlay(bossSettings.overlay());
        }
        viewer.showBossBar(bar);
    }

    void hide(Player viewer) {
        BossBar bar = bars.remove(viewer.getUniqueId());
        if (bar != null) {
            viewer.hideBossBar(bar);
        }
    }

    void hide(UUID viewerId) {
        BossBar bar = bars.remove(viewerId);
        Player viewer = bar == null ? null : Bukkit.getPlayer(viewerId);
        if (bar != null && viewer != null) {
            viewer.hideBossBar(bar);
        }
    }

    void hideAll() {
        for (UUID viewerId : new ArrayList<>(bars.keySet())) {
            hide(viewerId);
        }
    }

    static float progress(double health, double maxHealth) {
        return (float) HealthFormatter.healthRatio(health, maxHealth);
    }
}
