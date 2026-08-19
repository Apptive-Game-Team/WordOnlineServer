package com.wordonline.server.game.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class BotExecutorConfig {

    private ExecutorService botExecutorService;

    /**
     * Size and queue depth of the pool the bot ticks run on.
     *
     * <p>{@code poolSize} of zero, the default, means one thread per available processor.
     */
    @ConfigurationProperties(prefix = "bot.executor")
    public record BotExecutorProperties(Integer poolSize, Integer queueCapacity) {

        private static final int FALLBACK_QUEUE_CAPACITY = 64;

        public BotExecutorProperties {
            poolSize = poolSize == null || poolSize < 1
                    ? Runtime.getRuntime().availableProcessors()
                    : poolSize;
            queueCapacity = queueCapacity == null || queueCapacity < 1
                    ? FALLBACK_QUEUE_CAPACITY
                    : queueCapacity;
        }
    }

    /**
     * A fixed pool with a bounded queue, not a cached pool. A cached pool hands off through a
     * SynchronousQueue and starts a new thread whenever no idle one takes the task immediately,
     * which on a saturated core is most of the time: two bots per session and a hundred sessions
     * is up to two hundred bot threads on top of the loop threads, and a run queue that long is
     * what makes a loop thread miss its frame deadline after Thread.sleep wakes it.
     *
     * <p>The rejection policy has to be CallerRunsPolicy rather than a discard. BotAgentSystem
     * gates each bot with a CAS flag that its submitted task clears in a finally block, so a task
     * that never runs would leave that flag set and the bot would never act again. Running the
     * tick on the loop thread costs that frame instead, and pushes back on the thing producing
     * the work. Skipping or delaying one reaction interval is behaviour BotAgent already
     * documents as safe.
     */
    @Bean
    public ExecutorService botExecutorService(BotExecutorProperties properties) {
        int poolSize = properties.poolSize();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(properties.queueCapacity()),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("bot-executor-" + thread.getId());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
        log.info("[BotExecutor] Fixed pool of {} threads, queue capacity {}",
                poolSize, properties.queueCapacity());
        botExecutorService = executor;
        return executor;
    }

    @PreDestroy
    public void cleanup() {
        if (botExecutorService != null) {
            botExecutorService.shutdownNow();
            try {
                if (!botExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Bot executor service did not terminate within timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Bot executor service shutdown interrupted");
            }
        }
    }
}
