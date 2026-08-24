package com.wordonline.server.game.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.config.BotExecutorConfig.BotExecutorProperties;

class BotExecutorConfigTest {

    private final BotExecutorConfig botExecutorConfig = new BotExecutorConfig();

    @Test
    void thePoolIsFixedAndItsQueueIsBounded() {
        ExecutorService executorService =
                botExecutorConfig.botExecutorService(new BotExecutorProperties(2, 8));

        ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
        assertThat(pool.getCorePoolSize()).isEqualTo(2);
        assertThat(pool.getMaximumPoolSize()).isEqualTo(2);
        assertThat(pool.getQueue().remainingCapacity()).isEqualTo(8);

        botExecutorConfig.cleanup();
    }

    @Test
    void aFullQueueRunsTheTaskOnTheCallerInsteadOfDroppingIt() {
        // BotAgentSystem clears its per-bot CAS gate in the submitted task's finally block, so a
        // dropped task would wedge that bot for good.
        ExecutorService executorService =
                botExecutorConfig.botExecutorService(new BotExecutorProperties(1, 1));

        assertThat(((ThreadPoolExecutor) executorService).getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);

        botExecutorConfig.cleanup();
    }

    @Test
    void anUnsetPoolSizeIsOneThreadPerAvailableProcessor() {
        BotExecutorProperties properties = new BotExecutorProperties(null, null);

        assertThat(properties.poolSize()).isEqualTo(Runtime.getRuntime().availableProcessors());
        assertThat(properties.queueCapacity()).isEqualTo(64);
        assertThat(new BotExecutorProperties(0, 0).poolSize())
                .isEqualTo(Runtime.getRuntime().availableProcessors());
    }

    @Test
    void theThreadsAreDaemonsSoShutdownIsNotHeldUp() throws Exception {
        ExecutorService executorService =
                botExecutorConfig.botExecutorService(new BotExecutorProperties(1, 4));

        Thread[] worker = new Thread[1];
        executorService.submit(() -> worker[0] = Thread.currentThread()).get();

        assertThat(worker[0].isDaemon()).isTrue();
        assertThat(worker[0].getName()).startsWith("bot-executor-");

        botExecutorConfig.cleanup();
    }
}
