package dev.spa.hithealth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-thread-only memory of each viewer's latest displayed hit, used by PlaceholderAPI. */
final class LastHitTracker {

    record Hit(UUID targetId, double damage, long timeMillis) {
    }

    private final Map<UUID, Hit> hits = new HashMap<>();

    void record(UUID viewerId, UUID targetId, double damage, long timeMillis) {
        hits.put(viewerId, new Hit(targetId, damage, timeMillis));
    }

    /** Returns the last hit unless it is older than the configured timeout. */
    Hit find(UUID viewerId, long nowMillis, long timeoutMillis) {
        Hit hit = hits.get(viewerId);
        if (hit == null) {
            return null;
        }
        if (nowMillis - hit.timeMillis() > timeoutMillis) {
            hits.remove(viewerId);
            return null;
        }
        return hit;
    }

    void remove(UUID viewerId) {
        hits.remove(viewerId);
    }
}
