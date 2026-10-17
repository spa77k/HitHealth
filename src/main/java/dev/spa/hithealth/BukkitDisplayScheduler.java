package dev.spa.hithealth;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

final class BukkitDisplayScheduler implements HealthDisplayController.Scheduler {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    BukkitDisplayScheduler(Plugin plugin) {
        this.plugin = plugin;
        scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public HealthDisplayController.Task later(Runnable action, long delayTicks) {
        return scheduler.runTaskLater(plugin, action, delayTicks)::cancel;
    }

    @Override
    public HealthDisplayController.Task repeating(Runnable action, long delayTicks, long periodTicks) {
        return scheduler.runTaskTimer(plugin, action, delayTicks, periodTicks)::cancel;
    }
}
