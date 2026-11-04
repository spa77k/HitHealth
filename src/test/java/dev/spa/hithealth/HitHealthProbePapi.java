package dev.spa.hithealth;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * Isolated PlaceholderAPI calls, kept out of {@link HitHealthProbe} so the probe can load when
 * PlaceholderAPI is not installed.
 */
final class HitHealthProbePapi {

    private static final String PLACEHOLDERS =
            "%hithealth_enabled%|%hithealth_target_hp%|%hithealth_target_max%|"
                    + "%hithealth_target_percent%|%hithealth_target_bar%|%hithealth_target_name%";

    private HitHealthProbePapi() {
    }

    static String request(Player viewer) {
        return PlaceholderAPI.setPlaceholders(viewer, PLACEHOLDERS);
    }
}
