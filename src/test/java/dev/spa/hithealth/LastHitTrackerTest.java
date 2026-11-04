package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class LastHitTrackerTest {

    private final LastHitTracker tracker = new LastHitTracker();
    private final UUID viewer = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void recordsTheLatestHitPerViewer() {
        tracker.record(viewer, target, 3.5, 1_000);

        LastHitTracker.Hit hit = tracker.find(viewer, 1_000, 10_000);

        assertEquals(target, hit.targetId());
        assertEquals(3.5, hit.damage());
        assertEquals(1_000, hit.timeMillis());
    }

    @Test
    void hitsAtExactlyTheTimeoutAreStillReturned() {
        tracker.record(viewer, target, 3.5, 1_000);

        assertEquals(target, tracker.find(viewer, 11_000, 10_000).targetId());
        assertNull(tracker.find(viewer, 11_001, 10_000));
        assertNull(tracker.find(viewer, 11_002, 10_000));
    }

    @Test
    void removingAViewerForgetsTheHit() {
        tracker.record(viewer, target, 3.5, 1_000);
        tracker.remove(viewer);

        assertNull(tracker.find(viewer, 1_000, 10_000));
    }

    @Test
    void expiredRemovalDoesNotDeleteANewerHit() {
        tracker.record(viewer, target, 3.5, 1_000);
        LastHitTracker.Hit expired = tracker.find(viewer, 1_000, 10_000);
        UUID newerTarget = UUID.randomUUID();
        tracker.record(viewer, newerTarget, 7.0, 2_000);

        assertFalse(tracker.removeIfSame(viewer, expired));

        LastHitTracker.Hit current = tracker.find(viewer, 2_000, 10_000);
        assertEquals(newerTarget, current.targetId());
        assertEquals(7.0, current.damage());
    }

    @Test
    void snapshotUpdateOnlyAppliesToTheMatchingTarget() {
        tracker.record(viewer, target, 3.5, 1_000);
        UUID otherTarget = UUID.randomUUID();

        tracker.updateSnapshot(viewer, otherTarget, 4, 20, Component.text("Other"));

        assertNull(tracker.find(viewer, 1_000, 10_000).snapshot());

        tracker.updateSnapshot(viewer, target, 15, 20, Component.text("Zombie"));

        LastHitTracker.Snapshot snapshot = tracker.find(viewer, 1_000, 10_000).snapshot();
        assertEquals(15, snapshot.health());
        assertEquals(20, snapshot.maxHealth());
        assertEquals(Component.text("Zombie"), snapshot.name());
    }

    @Test
    void snapshotReadbackKeepsTheRecordedHitFields() {
        tracker.record(viewer, target, 3.5, 1_000);
        Component name = Component.text("Target");

        tracker.updateSnapshot(viewer, target, 15, 20, name);

        LastHitTracker.Hit hit = tracker.find(viewer, 1_001, 10_000);
        assertSame(name, hit.snapshot().name());
        assertEquals(target, hit.targetId());
        assertEquals(3.5, hit.damage());
        assertEquals(1_000, hit.timeMillis());
    }

    @Test
    void snapshotUpdateForAnUnknownViewerIsIgnored() {
        tracker.updateSnapshot(viewer, target, 15, 20, Component.text("Zombie"));

        assertNull(tracker.find(viewer, 1_000, 10_000));
    }

    @Test
    void expiredHitsAreRemovedFromTheTracker() {
        tracker.record(viewer, target, 3.5, 1_000);

        assertNull(tracker.find(viewer, 11_001, 10_000));
        assertNull(tracker.find(viewer, 11_002, 10_000));
    }
}
