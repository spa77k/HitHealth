package dev.spa.hithealth;

import java.util.Locale;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** Optional PlaceholderAPI bridge for the viewer's most recent displayed hit. */
public final class HitHealthPlaceholderExpansion extends PlaceholderExpansion {

    private final HitHealthPlugin plugin;

    HitHealthPlaceholderExpansion(HitHealthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "hithealth";
    }

    @Override
    public String getAuthor() {
        return "spa";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }
        Player viewer = player.getPlayer();
        if (viewer == null || !viewer.isOnline()) {
            return "";
        }
        String key = params.toLowerCase(Locale.ROOT);
        if (key.equals("enabled")) {
            return Boolean.toString(plugin.isEnabledFor(player.getUniqueId()));
        }
        if (!isTargetPlaceholder(key)) {
            return null;
        }
        HitHealthSettings settings = plugin.settings();
        LastHitTracker.Hit hit = plugin.lastHits().find(player.getUniqueId(), System.currentTimeMillis(),
                settings.placeholderTimeoutMillis());
        if (hit == null) {
            return "";
        }
        double health;
        double maxHealth;
        Component name;
        if (Bukkit.isPrimaryThread()) {
            Entity entity = Bukkit.getEntity(hit.targetId());
            health = 0;
            maxHealth = 0;
            if (entity instanceof LivingEntity living) {
                AttributeInstance maximum = living.getAttribute(Attribute.MAX_HEALTH);
                maxHealth = maximum != null ? maximum.getValue() : 0;
                if (living.isValid() && !living.isDead()) {
                    health = living.getHealth();
                }
            }
            name = entity != null ? entity.name() : Component.empty();
        } else {
            // Off the server thread, reading a live entity is unsafe. Use the values captured by
            // the display on its last server-thread render instead.
            LastHitTracker.Snapshot snapshot = hit.snapshot();
            health = snapshot != null ? snapshot.health() : 0;
            maxHealth = snapshot != null ? snapshot.maxHealth() : 0;
            name = snapshot != null ? snapshot.name() : Component.empty();
        }
        return switch (key) {
            case "target_name" -> targetName(name, viewer);
            case "target_hp" -> settings.formatter().formatNumber(health);
            case "target_max" -> settings.formatter().formatNumber(maxHealth);
            case "target_damage" -> settings.formatter().formatNumber(hit.damage());
            case "target_percent" -> Integer.toString(HealthFormatter.percent(health, maxHealth));
            case "target_bar" -> settings.barSettings().plain(health, maxHealth);
            default -> null;
        };
    }

    private static boolean isTargetPlaceholder(String key) {
        return switch (key) {
            case "target_name", "target_hp", "target_max", "target_damage", "target_percent", "target_bar" -> true;
            default -> false;
        };
    }

    private static String targetName(Component name, Player viewer) {
        if (name == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText()
                .serialize(GlobalTranslator.render(name, viewer.locale()));
    }
}
