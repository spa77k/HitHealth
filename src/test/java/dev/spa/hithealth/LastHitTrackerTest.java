package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
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
}
