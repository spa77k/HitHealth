package dev.spa.hithealth;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

/** Stores only disabled players; everyone else has the HP display enabled by default. */
final class PlayerPreferences {

    private final File file;
    private final Logger logger;
    private final Set<UUID> disabled = new HashSet<>();

    PlayerPreferences(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
        load();
    }

    boolean isEnabledFor(UUID player) {
        return !disabled.contains(player);
    }

    void setEnabledFor(UUID player, boolean enabled) {
        boolean changed = enabled ? disabled.remove(player) : disabled.add(player);
        if (changed) {
            save();
        }
    }

    private void load() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String id : yaml.getStringList("disabled")) {
            try {
                disabled.add(UUID.fromString(id));
            } catch (IllegalArgumentException ignored) {
                logger.warning("Ignored invalid UUID in players.yml: " + id);
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("disabled", disabled.stream().map(UUID::toString).sorted().toList());
        try {
            yaml.save(file);
        } catch (IOException error) {
            logger.log(Level.WARNING, "Could not save players.yml", error);
        }
    }
}
