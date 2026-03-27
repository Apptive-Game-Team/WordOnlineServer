package com.wordonline.server.game.domain.object.component.magic;

public final class VineSpawnContext {

    private static final ThreadLocal<VineHitTracker> CURRENT_TRACKER = new ThreadLocal<>();

    private VineSpawnContext() {
    }

    public static VineHitTracker currentTracker() {
        return CURRENT_TRACKER.get();
    }

    public static void runWithTracker(VineHitTracker tracker, Runnable action) {
        VineHitTracker previousTracker = CURRENT_TRACKER.get();
        CURRENT_TRACKER.set(tracker);
        try {
            action.run();
        } finally {
            if (previousTracker == null) {
                CURRENT_TRACKER.remove();
            } else {
                CURRENT_TRACKER.set(previousTracker);
            }
        }
    }
}
