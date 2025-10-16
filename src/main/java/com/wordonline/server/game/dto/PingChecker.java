package com.wordonline.server.game.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PingChecker {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Map<Long, ScheduledFuture<?>> pingTasks = new ConcurrentHashMap<>();
    private static final int PING_TIMEOUT_THRESHOLD = 20;

    private final Consumer<Long> onNonPing;

    public PingChecker(long userId1, long userId2, Consumer<Long> onNonPing) {
        ping(userId1);
        ping(userId2);
        this.onNonPing = onNonPing;
    }

    public void ping(long userId) {
        if(userId < 0) return;

        ScheduledFuture<?> existing = pingTasks.get(userId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            log.trace("{}초 동안 핑이 없음: userId: {}", PING_TIMEOUT_THRESHOLD, userId);
            pingTasks.remove(userId);
            onNonPing.accept(userId);
        }, PING_TIMEOUT_THRESHOLD, TimeUnit.SECONDS);

        pingTasks.put(userId, task);
    }

    private static final Logger log = LoggerFactory.getLogger(PingChecker.class);
}
