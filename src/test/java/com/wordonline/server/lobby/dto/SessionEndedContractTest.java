package com.wordonline.server.lobby.dto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the body of {@code POST /api/internal/game-sessions/{sessionId}/ended} against a
 * fixture shared with WordOnlineMatching, which keeps a byte-identical copy under the same
 * path.
 *
 * <p>A rename on one side alone would not throw at runtime: the lobby would read a null
 * instance id, decline to close the ticket, and leave the player's next queue attempt to be
 * rescued by the reconciler minutes later, with nothing in either log naming the cause.
 */
class SessionEndedContractTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    void notificationBodyMatchesTheSharedFixture() throws IOException {
        SessionEndedNotificationDto body =
                new SessionEndedNotificationDto("00000000-0000-0000-0000-000000000001");

        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(body)))
                .isEqualTo(objectMapper.readTree(fixture("session-ended-notification.json")));
    }

    @Test
    void notificationCarriesOnlyTheInstanceId() throws IOException {
        // The session id travels in the path, so the body stays a single field on purpose.
        JsonNode json = objectMapper.readTree(fixture("session-ended-notification.json"));

        assertThat(json.fieldNames()).toIterable().containsExactly("instanceId");
        assertThat(json.get("instanceId").isTextual()).isTrue();
    }

    private String fixture(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/contract/" + name)) {
            assertThat(in).as("missing fixture: %s", name).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
