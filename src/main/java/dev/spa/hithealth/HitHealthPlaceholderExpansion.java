package dev.spa.hithealth;

import java.util.Locale;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
        LastHitTracker.Hit hit = plugin.lastHits().find(player.getUniqueId(), System.currentTimeMillis(),
                plugin.settings().placeholderTimeoutMillis());
        if (hit == null) {
            return "";
        }
        Entity entity = Bukkit.getEntity(hit.targetId());
        double health = 0;
        double maxHealth = 0;
        if (entity instanceof LivingEntity living) {
            AttributeInstance maximum = living.getAttribute(Attribute.MAX_HEALTH);
            maxHealth = maximum != null ? maximum.getValue() : 0;
            if (living.isValid() && !living.isDead()) {
                health = living.getHealth();
            }
        }
        HitHealthSettings settings = plugin.settings();
        return switch (key) {
            case "target_name" -> targetName(entity, viewer);
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

    private static String targetName(Entity entity, Player viewer) {
        if (entity == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText()
                .serialize(GlobalTranslator.render(entity.name(), viewer.locale()));
    }
}
