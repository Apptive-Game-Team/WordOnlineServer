package com.wordonline.server.lobby.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.wordonline.server.lobby.config.LobbyProperties;
import com.wordonline.server.server.service.ServerInstanceIdProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LobbySessionClientTest {

    private static final Duration REQUEST_TIMEOUT = Duration.ofMillis(300);
    private static final int MAX_ATTEMPTS = 2;

    private final List<RecordedRequest> received = new CopyOnWriteArrayList<>();
    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicLong handlerDelayMillis = new AtomicLong(0);
    private final ServerInstanceIdProvider instanceIdProvider = new ServerInstanceIdProvider();

    private HttpServer lobbyServer;
    private ExecutorService lobbyExecutor;
    private String baseUrl;

    @TempDir
    Path tempDir;

    @BeforeEach
    void startFakeLobby() throws IOException {
        lobbyServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        lobbyExecutor = Executors.newFixedThreadPool(4);
        lobbyServer.setExecutor(lobbyExecutor);
        lobbyServer.createContext("/", this::handle);
        lobbyServer.start();
        baseUrl = "http://127.0.0.1:" + lobbyServer.getAddress().getPort();
    }

    @AfterEach
    void stopFakeLobby() {
        lobbyServer.stop(0);
        lobbyExecutor.shutdownNow();
    }

    @Test
    void postsSessionIdAndInstanceIdWithBearerToken() throws IOException {
        Path tokenFile = Files.writeString(tempDir.resolve("lobby.token"), "service-token-value\n");

        client(baseUrl, tokenFile.toString()).notifySessionEnded("session-1");

        assertThat(received).hasSize(1);
        RecordedRequest request = received.getFirst();
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo("/api/internal/game-sessions/session-1/ended");
        assertThat(request.authorization()).isEqualTo("Bearer service-token-value");
        assertThat(request.contentType()).contains("application/json");
        assertThat(request.body()).isEqualTo("{\"instanceId\":\"" + instanceIdProvider.getInstanceId() + "\"}");
    }

    @Test
    void notifiesWithoutAuthorizationWhenTokenPathIsUnset() {
        assertDoesNotThrow(() -> client(baseUrl, null).notifySessionEnded("session-1"));

        assertThat(received).hasSize(1);
        assertThat(received.getFirst().authorization()).isNull();
    }

    @Test
    void notifiesWithoutAuthorizationWhenTokenFileIsMissing() {
        String missingPath = tempDir.resolve("absent.token").toString();

        assertDoesNotThrow(() -> client(baseUrl, missingPath).notifySessionEnded("session-1"));

        assertThat(received).hasSize(1);
        assertThat(received.getFirst().authorization()).isNull();
    }

    @Test
    void doesNotThrowWhenLobbyDoesNotRespondInTime() {
        handlerDelayMillis.set(REQUEST_TIMEOUT.toMillis() * 5);

        long startedAt = System.nanoTime();
        assertDoesNotThrow(() -> client(baseUrl, null).notifySessionEnded("session-1"));
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

        // Bounded by the configured timeout per attempt, generously padded for CI scheduling.
        assertThat(elapsed).isLessThan(REQUEST_TIMEOUT.multipliedBy(MAX_ATTEMPTS * 4L));
    }

    @Test
    void retriesServerErrorsUpToTheConfiguredLimit() {
        responseStatus.set(503);

        assertDoesNotThrow(() -> client(baseUrl, null).notifySessionEnded("session-1"));

        assertThat(received).hasSize(MAX_ATTEMPTS);
    }

    @Test
    void doesNotRetryWhenTheLobbyRejectsTheRequest() {
        responseStatus.set(401);

        assertDoesNotThrow(() -> client(baseUrl, null).notifySessionEnded("session-1"));

        assertThat(received).hasSize(1);
    }

    @Test
    void skipsNotificationWhenBaseUrlIsUnset() {
        assertDoesNotThrow(() -> client(null, null).notifySessionEnded("session-1"));

        assertThat(received).isEmpty();
    }

    private LobbySessionClient client(String lobbyBaseUrl, String tokenPath) {
        LobbyProperties properties =
                new LobbyProperties(lobbyBaseUrl, tokenPath, REQUEST_TIMEOUT, MAX_ATTEMPTS);
        return new LobbySessionClient(properties, new LobbyServiceTokenProvider(properties), instanceIdProvider);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            received.add(new RecordedRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    new String(body.readAllBytes(), StandardCharsets.UTF_8)));
        }

        long delay = handlerDelayMillis.get();
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        exchange.sendResponseHeaders(responseStatus.get(), -1);
        try (OutputStream response = exchange.getResponseBody()) {
            response.flush();
        }
    }

    private record RecordedRequest(String method,
                                   String path,
                                   String authorization,
                                   String contentType,
                                   String body) {
    }
}
