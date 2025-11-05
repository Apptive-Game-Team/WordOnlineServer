package com.wordonline.server.game.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class BotExecutorConfig {

    private ExecutorService botExecutorService;

    @Bean
    public ExecutorService botExecutorService() {
        botExecutorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setName("bot-executor-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
        return botExecutorService;
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
