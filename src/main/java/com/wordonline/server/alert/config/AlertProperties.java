package com.wordonline.server.alert.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Operational alerts posted to Discord.
 *
 * <p>The webhook is the same one the log appender is configured with, so a deployment that
 * already sets {@code DISCORD_WEBHOOK_URL} needs no new configuration. An empty URL disables
 * alerting outright, which is what local runs and tests get.
 *
 * <p>{@link #cooldown()} is what keeps a degraded server from emptying itself into the channel:
 * a box holding fifty sessions below the threshold has one problem, not fifty, and it still has
 * that problem on the next sweep two seconds later.
 */
@ConfigurationProperties(prefix = "alert")
public record AlertProperties(
        Boolean enabled,
        String discordWebhookUrl,
        Double fpsThreshold,
        Duration cooldown) {

    private static final double FALLBACK_FPS_THRESHOLD = 15.0;
    private static final Duration FALLBACK_COOLDOWN = Duration.ofMinutes(5);

    public AlertProperties {
        enabled = enabled == null || enabled;
        discordWebhookUrl = discordWebhookUrl == null ? "" : discordWebhookUrl.trim();
        fpsThreshold = fpsThreshold == null ? FALLBACK_FPS_THRESHOLD : fpsThreshold;
        cooldown = cooldown == null ? FALLBACK_COOLDOWN : cooldown;
    }

    public boolean active() {
        return enabled && !discordWebhookUrl.isEmpty();
    }
}
