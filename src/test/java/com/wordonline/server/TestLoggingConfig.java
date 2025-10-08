package com.wordonline.server;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.wordonline.server.config.DiscordWebhookAppender;

import ch.qos.logback.classic.spi.ILoggingEvent;

@TestConfiguration
public class TestLoggingConfig {
    @Bean
    @Primary
    public DiscordWebhookAppender discordWebhookAppender() {
        return new DiscordWebhookAppender() {
            @Override
            protected void append(ILoggingEvent eventObject) {

            }
        };
    }
}