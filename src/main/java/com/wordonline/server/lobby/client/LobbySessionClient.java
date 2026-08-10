package com.wordonline.server.lobby.client;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.wordonline.server.lobby.config.LobbyProperties;
import com.wordonline.server.lobby.dto.SessionEndedNotificationDto;
import com.wordonline.server.server.service.ServerInstanceIdProvider;

/**
 * Outbound calls to the lobby about sessions owned by this process.
 *
 * <p>Match tickets live in the lobby's Redis, so a session that ends here leaves the ticket
 * stuck in {@code MATCHED} until the lobby is told. The lobby also runs a reconciler, which
 * makes this notification a latency optimisation rather than a correctness requirement:
 * every failure is swallowed and logged, and attempts are bounded by a short timeout so a
 * dead lobby cannot hold the game-loop teardown thread open.
 */
@Service
public class LobbySessionClient {

    private static final Logger log = LoggerFactory.getLogger(LobbySessionClient.class);
    private static final String SESSION_ENDED_PATH = "/api/internal/game-sessions/{sessionId}/ended";

    private final LobbyProperties lobbyProperties;
    private final LobbyServiceTokenProvider tokenProvider;
    private final ServerInstanceIdProvider instanceIdProvider;
    private final RestClient restClient;

    public LobbySessionClient(LobbyProperties lobbyProperties,
                              LobbyServiceTokenProvider tokenProvider,
                              ServerInstanceIdProvider instanceIdProvider) {
        this.lobbyProperties = lobbyProperties;
        this.tokenProvider = tokenProvider;
        this.instanceIdProvider = instanceIdProvider;
        this.restClient = RestClient.builder()
                .requestFactory(timeBoundRequestFactory(lobbyProperties))
                .build();
    }

    /**
     * Tells the lobby that a session finished here so it can close the match ticket.
     *
     * <p>Never throws: session teardown must complete even when the lobby is unreachable.
     */
    public void notifySessionEnded(String sessionId) {
        if (!lobbyProperties.hasBaseUrl()) {
            log.warn("[Lobby] Base URL is not configured; skipping session-ended notification; sessionId: {}",
                    sessionId);
            return;
        }

        URI uri = sessionEndedUri(sessionId);
        SessionEndedNotificationDto body = new SessionEndedNotificationDto(instanceIdProvider.getInstanceId());
        int maxAttempts = lobbyProperties.maxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                post(uri, body);
                log.info("[Lobby] Session-ended notification accepted; sessionId: {}", sessionId);
                return;
            } catch (HttpClientErrorException e) {
                // The lobby rejected the request itself; retrying the same payload cannot help.
                log.warn("[Lobby] Session-ended notification rejected; sessionId: {}, status: {}",
                        sessionId, e.getStatusCode(), e);
                return;
            } catch (Exception e) {
                log.warn("[Lobby] Session-ended notification failed; sessionId: {}, attempt: {}/{}",
                        sessionId, attempt, maxAttempts, e);
            }
        }

        log.warn("[Lobby] Giving up on session-ended notification; sessionId: {}, the lobby reconciler must "
                + "close the ticket", sessionId);
    }

    private void post(URI uri, SessionEndedNotificationDto body) {
        Optional<String> token = tokenProvider.getToken();
        restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> token.ifPresent(value ->
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + value)))
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private URI sessionEndedUri(String sessionId) {
        return UriComponentsBuilder.fromUriString(lobbyProperties.baseUrl())
                .path(SESSION_ENDED_PATH)
                .buildAndExpand(sessionId)
                .encode()
                .toUri();
    }

    private static SimpleClientHttpRequestFactory timeBoundRequestFactory(LobbyProperties lobbyProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(lobbyProperties.requestTimeout());
        requestFactory.setReadTimeout(lobbyProperties.requestTimeout());
        return requestFactory;
    }
}
