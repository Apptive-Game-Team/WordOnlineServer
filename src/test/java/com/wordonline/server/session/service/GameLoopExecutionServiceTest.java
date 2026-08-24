package com.wordonline.server.session.service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.MmrService;
import com.wordonline.server.game.service.UserService;
import com.wordonline.server.session.config.GameLoopExecutionProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

class GameLoopExecutionServiceTest {

    private GameLoopExecutionService executionService;

    @AfterEach
    void tearDown() {
        if (executionService != null) {
            executionService.shutdown();
        }
    }

    @Test
    void actorModeUsesConfiguredBoundedWorkerPool() {
        executionService = new GameLoopExecutionService(new GameLoopExecutionProperties(
                GameLoopExecutionProperties.Mode.ACTOR, 2));

        assertThat(executionService.actorPoolSize()).isEqualTo(2);
    }

    @Test
    void actorTicksNeverOverlapForOneSession() {
        AtomicBoolean insideFrame = new AtomicBoolean();
        AtomicBoolean overlapped = new AtomicBoolean();
        AtomicInteger frames = new AtomicInteger();
        GameLoop loop = loopThat(() -> {
            if (!insideFrame.compareAndSet(false, true)) {
                overlapped.set(true);
            }
            try {
                Thread.sleep(80);
                frames.incrementAndGet();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                insideFrame.set(false);
            }
        });
        executionService = new GameLoopExecutionService(new GameLoopExecutionProperties(
                GameLoopExecutionProperties.Mode.ACTOR, 2));

        executionService.start(loop);

        await().atMost(Duration.ofSeconds(2)).until(() -> frames.get() >= 3);
        loop.close();
        await().atMost(Duration.ofSeconds(1)).until(() -> !loop.is_running());
        assertThat(overlapped).isFalse();
    }

    @Test
    void separateActorsShareConfiguredWorker() {
        CountDownLatch bothTicked = new CountDownLatch(2);
        GameLoop first = loopThat(bothTicked::countDown);
        GameLoop second = loopThat(bothTicked::countDown);
        executionService = new GameLoopExecutionService(new GameLoopExecutionProperties(
                GameLoopExecutionProperties.Mode.ACTOR, 1));

        executionService.start(first);
        executionService.start(second);

        await().atMost(Duration.ofSeconds(1)).until(() -> bothTicked.getCount() == 0);
        first.close();
        second.close();
    }

    private static GameLoop loopThat(Runnable update) {
        return new GameLoop(mock(MmrService.class), mock(UserService.class),
                mock(GameContext.class), mock(Parameters.class)) {
            @Override
            protected void update() {
                update.run();
            }
        };
    }
}
