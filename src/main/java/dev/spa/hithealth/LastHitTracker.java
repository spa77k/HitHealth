package dev.spa.hithealth;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;

/**
 * Thread-safe memory of each viewer's latest displayed hit, used by PlaceholderAPI.
 *
 * <p>The hit itself is recorded on the server thread, but PlaceholderAPI may read it from an
 * asynchronous thread. Health and name values needed off the server thread are snapshotted on the
 * server thread whenever the display renders.</p>
 */
final class LastHitTracker {

    record Snapshot(double health, double maxHealth, Component name) {
    }

    record Hit(UUID targetId, double damage, long timeMillis, Snapshot snapshot) {

        Hit(UUID targetId, double damage, long timeMillis) {
            this(targetId, damage, timeMillis, null);
        }
    }

    private final ConcurrentHashMap<UUID, Hit> hits = new ConcurrentHashMap<>();

    void record(UUID viewerId, UUID targetId, double damage, long timeMillis) {
        hits.put(viewerId, new Hit(targetId, damage, timeMillis));
    }

    /**
     * Stores the values rendered for the viewer's current hit. A stale render from an older target
     * never overwrites the snapshot of a newer hit.
     */
    void updateSnapshot(UUID viewerId, UUID targetId, double health, double maxHealth, Component name) {
        hits.computeIfPresent(viewerId, (ignored, hit) -> hit.targetId().equals(targetId)
                ? new Hit(hit.targetId(), hit.damage(), hit.timeMillis(),
                        new Snapshot(health, maxHealth, name))
                : hit);
    }

    /** Returns the last hit unless it is older than the configured timeout. */
    Hit find(UUID viewerId, long nowMillis, long timeoutMillis) {
        Hit hit = hits.get(viewerId);
        if (hit == null) {
            return null;
        }
        if (nowMillis - hit.timeMillis() > timeoutMillis) {
            removeIfSame(viewerId, hit);
            return null;
        }
        return hit;
    }

    /**
     * Removes only the exact hit that was found expired, so a newer hit recorded concurrently by
     * the server thread is kept.
     */
    boolean removeIfSame(UUID viewerId, Hit hit) {
        return hits.remove(viewerId, hit);
    }

    void remove(UUID viewerId) {
        hits.remove(viewerId);
    }
}
