package dev.spa.hithealth;

import java.io.File;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public final class HitHealthPlugin extends JavaPlugin {

    private HitHealthSettings settings;
    private PlayerPreferences preferences;
    private HealthDisplayController displays;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();
        preferences = new PlayerPreferences(new File(getDataFolder(), "players.yml"), getLogger());
        displays = new HealthDisplayController(new BukkitDisplayScheduler(this));
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        HitHealthCommand command = new HitHealthCommand(this);
        getCommand("hithealth").setExecutor(command);
        getCommand("hithealth").setTabCompleter(command);
    }

    public void reloadSettings() {
        reloadConfig();
        settings = HitHealthSettings.load(getConfig(), getLogger());
        if (displays != null) {
            displays.cancelAll();
        }
    }

    @Override
    public void onDisable() {
        if (displays != null) {
            displays.cancelAll();
        }
    }

    public HealthFormatter formatter() {
        return settings.formatter();
    }

    public boolean showPlayers() {
        return settings.showPlayers();
    }

    HitHealthSettings settings() {
        return settings;
    }

    HealthDisplayController displays() {
        return displays;
    }

    public boolean isEnabledFor(UUID player) {
        return preferences.isEnabledFor(player);
    }

    public void setEnabledFor(UUID player, boolean enabled) {
        preferences.setEnabledFor(player, enabled);
        if (!enabled) {
            displays.cancel(player);
        }
    }
}
