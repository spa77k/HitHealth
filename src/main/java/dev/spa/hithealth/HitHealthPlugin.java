package dev.spa.hithealth;

import java.io.File;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public final class HitHealthPlugin extends JavaPlugin {

    private volatile HitHealthSettings settings;
    private PlayerPreferences preferences;
    private HealthDisplayController displays;
    private BossBarDisplay bossBars;
    private LastHitTracker lastHits;
    private PlaceholderApiHook placeholderApi;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bossBars = new BossBarDisplay();
        lastHits = new LastHitTracker();
        reloadSettings();
        preferences = new PlayerPreferences(new File(getDataFolder(), "players.yml"), getLogger());
        displays = new HealthDisplayController(new BukkitDisplayScheduler(this));
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        HitHealthCommand command = new HitHealthCommand(this);
        getCommand("hithealth").setExecutor(command);
        getCommand("hithealth").setTabCompleter(command);
        registerPlaceholderApi();
    }

    public void reloadSettings() {
        reloadConfig();
        settings = HitHealthSettings.load(getConfig(), getLogger());
        if (displays != null) {
            displays.cancelAll();
        }
        if (bossBars != null) {
            bossBars.hideAll();
        }
    }

    @Override
    public void onDisable() {
        if (displays != null) {
            displays.cancelAll();
        }
        if (bossBars != null) {
            bossBars.hideAll();
        }
        if (placeholderApi != null) {
            placeholderApi.unregister();
            placeholderApi = null;
        }
    }

    private void registerPlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderApi = PlaceholderApiHook.install(this);
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

    BossBarDisplay bossBars() {
        return bossBars;
    }

    LastHitTracker lastHits() {
        return lastHits;
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
