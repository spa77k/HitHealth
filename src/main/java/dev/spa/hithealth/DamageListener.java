package dev.spa.hithealth;

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

/** 攻撃のあと、攻撃したプレイヤーに相手の残りHPを見せる。 */
public final class DamageListener implements Listener {

    private final HitHealthPlugin plugin;

    public DamageListener(HitHealthPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target) || target instanceof ArmorStand) {
            return;
        }
        if (!canShow(target)) {
            return;
        }
        Player viewer = attacker(event.getDamager());
        if (viewer == null || viewer.equals(target)
                || !viewer.hasPermission("hithealth.use") || !plugin.isEnabledFor(viewer.getUniqueId())) {
            return;
        }
        // Capture this hit before the target's health changes. Absorbed damage is
        // already excluded by getFinalDamage(), and overkill cannot remove extra HP.
        double damage = healthDamage(target.getHealth(), event.getFinalDamage());
        plugin.displays().show(viewer.getUniqueId(), plugin.settings().displayDurationTicks(),
                () -> viewer.isOnline() && viewer.hasPermission("hithealth.use")
                        && plugin.isEnabledFor(viewer.getUniqueId()) && canShow(target),
                () -> show(viewer, target, damage),
                () -> {
                    if (viewer.isOnline()) {
                        viewer.sendActionBar(Component.empty());
                    }
                });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.displays().cancel(event.getPlayer().getUniqueId());
    }

    static double healthDamage(double health, double finalDamage) {
        return Double.isFinite(health) && Double.isFinite(finalDamage)
                ? Math.max(0, Math.min(health, finalDamage)) : 0;
    }

    private boolean canShow(LivingEntity target) {
        return (!(target instanceof Player) || plugin.showPlayers())
                && !plugin.settings().isExcluded(target.getWorld().getName(), target.getType());
    }

    static Player attacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
            if (shooter instanceof Entity entity) {
                return attacker(entity);
            }
            return null;
        }
        if (damager instanceof Tameable pet) {
            AnimalTamer owner = pet.getOwner();
            return owner instanceof Player player && player.isOnline() ? player : null;
        }
        return null;
    }

    private void show(Player viewer, LivingEntity target, double damage) {
        if (!viewer.isOnline()) {
            return;
        }
        double health = target.isValid() && !target.isDead() ? target.getHealth() : 0;
        AttributeInstance max = target.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = max != null ? max.getValue() : health;
        viewer.sendActionBar(plugin.formatter().render(target.name(), health, maxHealth, damage));
    }
}
