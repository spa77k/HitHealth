package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class HealthDisplayControllerTest {

    private final FakeScheduler scheduler = new FakeScheduler();
    private final HealthDisplayController controller = new HealthDisplayController(scheduler);
    private final UUID viewer = UUID.randomUUID();
    private final List<String> output = new ArrayList<>();

    @Test
    void defaultDisplayRunsOnceOnTheNextTickWithoutClearing() {
        show(viewer, "first", 0);
        assertEquals(List.of(), output);

        scheduler.advance(1);
        assertEquals(List.of("first"), output);
        scheduler.advance(100);
        controller.cancelAll();
        assertEquals(List.of("first"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void defaultModePreservesEveryHitEvenWithinOneTick() {
        show(viewer, "first", 0);
        show(viewer, "second", 0);

        scheduler.advance(1);

        assertEquals(List.of("first", "second"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void extendedDisplayRefreshesEverySecondAndClearsAtExpiry() {
        show(viewer, "target", 25);

        scheduler.advance(1);
        assertEquals(List.of("target"), output);
        scheduler.advance(19);
        assertEquals(List.of("target"), output);
        scheduler.advance(1);
        assertEquals(List.of("target", "target"), output);
        scheduler.advance(5);
        assertEquals(List.of("target", "target", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void subSecondDurationClearsWithoutWaitingForTheNextRefresh() {
        show(viewer, "target", 1);

        scheduler.advance(1);
        assertEquals(List.of("target"), output);
        scheduler.advance(1);
        assertEquals(List.of("target", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void repeatedHitsReplaceTheTargetAndRestartTheDurationWithoutClearing() {
        show(viewer, "old", 25);
        scheduler.advance(10);
        show(viewer, "new", 25);

        scheduler.advance(16);
        assertEquals(List.of("old", "new"), output);
        scheduler.advance(10);
        assertEquals(List.of("old", "new", "new", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void repeatedHitsBeforeTheFirstTickOnlyRenderTheLatestExtendedDisplay() {
        show(viewer, "old", 25);
        show(viewer, "new", 25);

        scheduler.advance(26);

        assertEquals(List.of("new", "new", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void cancellingAReplacementBeforeItRendersClearsThePreviousVisibleDisplay() {
        show(viewer, "old", 25);
        scheduler.advance(1);
        show(viewer, "new", 25);

        controller.cancel(viewer);
        scheduler.advance(100);

        assertEquals(List.of("old", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void anInactiveReplacementClearsThePreviousVisibleDisplay() {
        show(viewer, "old", 25);
        scheduler.advance(1);
        controller.show(viewer, 25, () -> false, () -> output.add("new"), () -> output.add("clear"));

        scheduler.advance(100);

        assertEquals(List.of("old", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void cancellationBeforeRenderingPreventsBothOutputAndClearing() {
        show(viewer, "first", 0);
        show(viewer, "second", 0);
        controller.cancel(viewer);
        show(viewer, "extended", 40);
        controller.cancel(viewer);

        scheduler.advance(100);

        assertEquals(List.of(), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void becomingInactiveStopsRefreshesAndClearsAnExtendedDisplay() {
        AtomicBoolean active = new AtomicBoolean(true);
        controller.show(viewer, 100, active::get, () -> output.add("target"), () -> output.add("clear"));
        scheduler.advance(1);
        active.set(false);

        scheduler.advance(100);

        assertEquals(List.of("target", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void inactiveViewersNeverRenderPendingDisplays() {
        controller.show(viewer, 0, () -> false, () -> output.add("single"), () -> output.add("clear"));
        controller.show(UUID.randomUUID(), 20, () -> false,
                () -> output.add("extended"), () -> output.add("clear"));

        scheduler.advance(100);

        assertEquals(List.of(), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void cancelAllClearsActiveDisplaysAndCancelsPendingWorkOnlyOnce() {
        show(viewer, "active", 100);
        scheduler.advance(1);
        show(UUID.randomUUID(), "pending", 100);
        show(UUID.randomUUID(), "one-shot", 0);

        controller.cancelAll();
        controller.cancelAll();
        scheduler.advance(100);

        assertEquals(List.of("active", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void cancellingOneViewerDoesNotAffectAnother() {
        UUID other = UUID.randomUUID();
        show(viewer, "first", 25);
        show(other, "second", 25);
        scheduler.advance(1);

        controller.cancel(viewer);
        scheduler.advance(25);

        assertEquals(List.of("first", "second", "clear", "second", "clear"), output);
        assertEquals(0, scheduler.activeTasks());
    }

    @Test
    void cancellingAnAlreadyExpiredDisplayHasNoEffect() {
        show(viewer, "target", 1);
        scheduler.advance(2);

        controller.cancel(viewer);
        controller.cancelAll();

        assertEquals(List.of("target", "clear"), output);
    }

    private void show(UUID player, String text, long duration) {
        controller.show(player, duration, () -> true, () -> output.add(text), () -> output.add("clear"));
    }

    private static final class FakeScheduler implements HealthDisplayController.Scheduler {
        private final List<ScheduledTask> tasks = new ArrayList<>();
        private long tick;

        @Override
        public HealthDisplayController.Task later(Runnable action, long delayTicks) {
            return schedule(action, delayTicks, 0);
        }

        @Override
        public HealthDisplayController.Task repeating(Runnable action, long delayTicks, long periodTicks) {
            return schedule(action, delayTicks, periodTicks);
        }

        private HealthDisplayController.Task schedule(Runnable action, long delay, long period) {
            ScheduledTask task = new ScheduledTask(action, tick + delay, period);
            tasks.add(task);
            return () -> task.cancelled = true;
        }

        private void advance(long ticks) {
            for (long i = 0; i < ticks; i++) {
                tick++;
                for (ScheduledTask task : new ArrayList<>(tasks)) {
                    if (!task.cancelled && task.due <= tick) {
                        task.action.run();
                        if (task.period == 0) {
                            task.cancelled = true;
                        } else {
                            task.due += task.period;
                        }
                    }
                }
            }
        }

        private long activeTasks() {
            return tasks.stream().filter(task -> !task.cancelled).count();
        }
    }

    private static final class ScheduledTask {
        private final Runnable action;
        private final long period;
        private long due;
        private boolean cancelled;

        private ScheduledTask(Runnable action, long due, long period) {
            this.action = action;
            this.due = due;
            this.period = period;
        }
    }
}
