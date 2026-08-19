package com.wordonline.server.alert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.wordonline.server.alert.config.AlertProperties;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Posts a line of text to the Discord webhook.
 *
 * <p>Callers are monitoring threads whose job is to keep sweeping, so the request never runs on
 * the calling thread and the queue is bounded: when Discord is slow or down, alerts are dropped
 * rather than allowed to pile up behind a scheduler. Delivery is therefore best effort by
 * design - the log, not this channel, is the record of what happened.
 */
@Slf4j
@Component
public class DiscordNotifier {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final int QUEUE_CAPACITY = 16;

    private final AlertProperties alertProperties;
    private final HttpClient httpClient;
    private final ThreadPoolExecutor sender;

    public DiscordNotifier(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
        this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
        this.sender = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                runnable -> {
                    Thread thread = new Thread(runnable, "discord-notifier");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardPolicy());
    }

    public boolean isEnabled() {
        return alertProperties.active();
    }

    public void send(String content) {
        if (!isEnabled()) {
            return;
        }
        sender.execute(() -> post(content));
    }

    private void post(String content) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(alertProperties.discordWebhookUrl()))
                    .header("Content-Type", "application/json")
                    .timeout(REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(payload(content)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.warn("[Alert] Discord webhook rejected the message; status: {}", response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[Alert] Discord webhook post failed: {}", e.toString());
        }
    }

    private String payload(String content) {
        return "{\"content\":\"" + escapeJson(content) + "\"}";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    @PreDestroy
    public void shutdown() {
        sender.shutdownNow();
    }
}
