package com.wordonline.server.lobby.config;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Binds the same keys {@code application.yml} declares. Nothing else in this suite starts a
 * Spring context, so without this a mistyped key or an unconvertible duration would only
 * surface as a failed boot in a deployed environment.
 */
class LobbyPropertiesTest {

    @Test
    void bindsTheKeysDeclaredInApplicationYml() {
        LobbyProperties properties = bind(Map.of(
                "lobby.base-url", "http://lobby:8080",
                "lobby.service-token-path", "/run/secrets/lobby.token",
                "lobby.request-timeout", "1000",
                "lobby.max-attempts", "2"));

        assertThat(properties.baseUrl()).isEqualTo("http://lobby:8080");
        assertThat(properties.serviceTokenPath()).isEqualTo("/run/secrets/lobby.token");
        assertThat(properties.requestTimeout()).isEqualTo(Duration.ofMillis(1000));
        assertThat(properties.maxAttempts()).isEqualTo(2);
        assertThat(properties.hasBaseUrl()).isTrue();
    }

    @Test
    void unsetEnvironmentLeavesTheNotificationDisabledInsteadOfBroken() {
        LobbyProperties properties = bind(Map.of(
                "lobby.base-url", "",
                "lobby.service-token-path", ""));

        assertThat(properties.hasBaseUrl()).isFalse();
        assertThat(properties.serviceTokenPath()).isEmpty();
        assertThat(properties.requestTimeout()).isPositive();
        assertThat(properties.maxAttempts()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void trailingSlashesInBaseUrlDoNotDoubleUpOnThePath() {
        LobbyProperties properties = bind(Map.of("lobby.base-url", "http://lobby:8080/"));

        assertThat(properties.baseUrl()).isEqualTo("http://lobby:8080");
    }

    private LobbyProperties bind(Map<String, Object> source) {
        return new Binder(new MapConfigurationPropertySource(source))
                .bind("lobby", LobbyProperties.class)
                .orElseThrow(() -> new AssertionError("lobby properties did not bind"));
    }
}
