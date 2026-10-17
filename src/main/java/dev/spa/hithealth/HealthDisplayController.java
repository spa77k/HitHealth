package dev.spa.hithealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Tracks one-shot work and owns at most one extended display per viewer on the server thread. */
final class HealthDisplayController {

    interface Task {
        void cancel();
    }

    interface Scheduler {
        Task later(Runnable action, long delayTicks);

        Task repeating(Runnable action, long delayTicks, long periodTicks);
    }

    private final Scheduler scheduler;
    private final Map<UUID, Set<Display>> displays = new HashMap<>();

    HealthDisplayController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    void show(UUID viewer, long durationTicks, BooleanSupplier active, Runnable render, Runnable clear) {
        boolean alreadyShown = durationTicks > 0 && cancel(viewer, false);
        Display display = new Display(active, render, clear);
        // A replacement still owns the previous text until its first render.
        display.shown = alreadyShown;
        displays.computeIfAbsent(viewer, ignored -> new LinkedHashSet<>()).add(display);
        if (durationTicks <= 0) {
            display.refresh = scheduler.later(() -> {
                if (!contains(viewer, display)) {
                    return;
                }
                remove(viewer, display);
                if (display.active.getAsBoolean()) {
                    display.render.run();
                }
            }, 1);
        } else {
            display.refresh = scheduler.repeating(() -> {
                if (!contains(viewer, display)) {
                    return;
                }
                if (!display.active.getAsBoolean()) {
                    cancel(viewer);
                    return;
                }
                display.render.run();
                display.shown = true;
            }, 1, 20);
            display.expiration = scheduler.later(() -> {
                if (contains(viewer, display)) {
                    cancel(viewer);
                }
            }, durationTicks + 1);
        }
    }

    void cancel(UUID viewer) {
        cancel(viewer, true);
    }

    void cancelAll() {
        for (UUID viewer : new ArrayList<>(displays.keySet())) {
            cancel(viewer);
        }
    }

    private boolean cancel(UUID viewer, boolean clear) {
        Set<Display> removed = displays.remove(viewer);
        if (removed == null) {
            return false;
        }
        boolean shown = false;
        for (Display display : removed) {
            shown |= display.shown;
            if (display.refresh != null) {
                display.refresh.cancel();
            }
            if (display.expiration != null) {
                display.expiration.cancel();
            }
            if (clear && display.shown) {
                display.clear.run();
            }
        }
        return shown;
    }

    private boolean contains(UUID viewer, Display display) {
        Set<Display> current = displays.get(viewer);
        return current != null && current.contains(display);
    }

    private void remove(UUID viewer, Display display) {
        Set<Display> current = displays.get(viewer);
        current.remove(display);
        if (current.isEmpty()) {
            displays.remove(viewer);
        }
    }

    private static final class Display {
        private final BooleanSupplier active;
        private final Runnable render;
        private final Runnable clear;
        private Task refresh;
        private Task expiration;
        private boolean shown;

        private Display(BooleanSupplier active, Runnable render, Runnable clear) {
            this.active = active;
            this.render = render;
            this.clear = clear;
        }
    }
}
