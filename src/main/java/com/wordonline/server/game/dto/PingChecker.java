package com.wordonline.server.game.dto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PingChecker {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Map<Long, ScheduledFuture<?>> pingTasks = new ConcurrentHashMap<>();
    private static final int FIRST_PING_TIMEOUT_THRESHOLD = 30;
    private static final int PING_TIMEOUT_THRESHOLD = 10;

    private final Consumer<Long> onNonPing;

    public PingChecker(long userId1, long userId2, Consumer<Long> onNonPing) {
        this.onNonPing = onNonPing;
        ping(userId1, FIRST_PING_TIMEOUT_THRESHOLD);
        ping(userId2, FIRST_PING_TIMEOUT_THRESHOLD);
    }

    public void ping(long userId) {
        ping(userId, PING_TIMEOUT_THRESHOLD);
    }

    public void ping(long userId, int threshold) {
        if (userId < 0) return;

        ScheduledFuture<?> existing = pingTasks.get(userId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            log.trace("No ping for {} seconds: userId={}", threshold, userId);
            pingTasks.remove(userId);
            onNonPing.accept(userId);
        }, PING_TIMEOUT_THRESHOLD, TimeUnit.SECONDS);

        pingTasks.put(userId, task);
    }
}
