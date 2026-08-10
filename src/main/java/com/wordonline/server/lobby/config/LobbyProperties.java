package com.wordonline.server.lobby.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

/**
 * Settings for outbound calls from this game server to the lobby.
 *
 * <p>The lobby keeps match ticket state in Redis, so a finished session has to be reported
 * over HTTP. Everything here is environment-driven: no URL and no credential belongs in
 * source. When {@link #baseUrl()} or {@link #serviceTokenPath()} is missing the caller is
 * expected to degrade to a log line rather than fail, because a lost notification is
 * recoverable through the lobby's reconciler while a broken session teardown is not.
 */
@ConfigurationProperties(prefix = "lobby")
public record LobbyProperties(
        String baseUrl,
        String serviceTokenPath,
        @DurationUnit(ChronoUnit.MILLIS) Duration requestTimeout,
        Integer maxAttempts) {

    private static final Duration FALLBACK_REQUEST_TIMEOUT = Duration.ofMillis(1000);
    private static final int FALLBACK_MAX_ATTEMPTS = 2;

    public LobbyProperties {
        baseUrl = stripTrailingSlashes(baseUrl);
        requestTimeout = requestTimeout == null || requestTimeout.isNegative() || requestTimeout.isZero()
                ? FALLBACK_REQUEST_TIMEOUT
                : requestTimeout;
        maxAttempts = maxAttempts == null || maxAttempts < 1 ? FALLBACK_MAX_ATTEMPTS : maxAttempts;
    }

    /**
     * @return whether a lobby endpoint is configured at all; without one nothing can be sent.
     */
    public boolean hasBaseUrl() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    private static String stripTrailingSlashes(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
