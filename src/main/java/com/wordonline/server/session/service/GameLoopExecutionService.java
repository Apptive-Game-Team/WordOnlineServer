package com.wordonline.server.session.service;

import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.session.config.GameLoopExecutionProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GameLoopExecutionService {

    private static final long FRAME_PERIOD_MILLIS = 1_000L / GameLoop.FPS;

    private final GameLoopExecutionProperties properties;
    private final ScheduledThreadPoolExecutor actorExecutor;

    public GameLoopExecutionService(GameLoopExecutionProperties properties) {
        this.properties = properties;
        this.actorExecutor = new ScheduledThreadPoolExecutor(
                properties.actorThreads(), actorThreadFactory());
        this.actorExecutor.setRemoveOnCancelPolicy(true);
        this.actorExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.actorExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    }

    public void start(GameLoop gameLoop) {
        if (properties.mode() == GameLoopExecutionProperties.Mode.DEDICATED_THREAD) {
            new Thread(gameLoop, "game-loop-dedicated").start();
            return;
        }

        ActorTickTask task = new ActorTickTask(gameLoop);
        ScheduledFuture<?> future = actorExecutor.scheduleAtFixedRate(
                task, 0, FRAME_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
        task.setFuture(future);
    }

    @PreDestroy
    void shutdown() {
        actorExecutor.shutdownNow();
    }

    int actorPoolSize() {
        return actorExecutor.getCorePoolSize();
    }

    private static ThreadFactory actorThreadFactory() {
        AtomicInteger sequence = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, "game-loop-actor-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    private static final class ActorTickTask implements Runnable {

        private final GameLoop gameLoop;
        private volatile ScheduledFuture<?> future;
        private volatile boolean stopped;

        private ActorTickTask(GameLoop gameLoop) {
            this.gameLoop = gameLoop;
        }

        @Override
        public void run() {
            if (gameLoop.runActorTick()) {
                return;
            }

            stopped = true;
            ScheduledFuture<?> scheduledFuture = future;
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
        }

        private void setFuture(ScheduledFuture<?> future) {
            this.future = future;
            if (stopped) {
                future.cancel(false);
            }
        }
    }
}
