package com.wordonline.server.game.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

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
            botExecutorService.shutdown();
        }
    }
}
