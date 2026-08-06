package com.wordonline.server.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.wordonline.server.server.entity.Server;
import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.entity.ServerType;
import com.wordonline.server.server.repository.ServerRepository;

@ExtendWith(MockitoExtension.class)
class ServerStatusServiceTest {

    @Mock
    private ServerRepository serverRepository;

    private ServerStatusService serverStatusService;

    @BeforeEach
    void setUp() {
        serverStatusService = new ServerStatusService(serverRepository);
        ReflectionTestUtils.setField(serverStatusService, "port", 7777);
        ReflectionTestUtils.setField(serverStatusService, "domain", "game.example.com");
        ReflectionTestUtils.setField(serverStatusService, "protocol", "https");
        ReflectionTestUtils.setField(serverStatusService, "maxSessions", 64);
    }

    @Test
    void publishHeartbeatCreatesServerWithCapacityAndTimestamp() {
        when(serverRepository.findByDomainAndPort("game.example.com", 7777)).thenReturn(Optional.empty());
        Instant before = Instant.now();

        serverStatusService.publishHeartbeat(7);

        ArgumentCaptor<Server> captor = ArgumentCaptor.forClass(Server.class);
        verify(serverRepository).save(captor.capture());
        Server saved = captor.getValue();
        assertThat(saved.getProtocol()).isEqualTo("https");
        assertThat(saved.getDomain()).isEqualTo("game.example.com");
        assertThat(saved.getPort()).isEqualTo(7777);
        assertThat(saved.getType()).isEqualTo(ServerType.GAME);
        assertThat(saved.getState()).isEqualTo(ServerState.ACTIVE);
        assertThat(saved.getSessionCount()).isEqualTo(7);
        assertThat(saved.getMaxSessions()).isEqualTo(64);
        assertThat(saved.getLastHeartbeatAt()).isAfterOrEqualTo(before);
    }

    @Test
    void inactiveStatusClearsSessionCount() {
        Server server = new Server("https", "game.example.com", 7777, ServerType.GAME, ServerState.ACTIVE);
        server.setSessionCount(5);
        when(serverRepository.findByDomainAndPort("game.example.com", 7777)).thenReturn(Optional.of(server));

        serverStatusService.setServerStatus(ServerState.INACTIVE);

        assertThat(server.getState()).isEqualTo(ServerState.INACTIVE);
        assertThat(server.getSessionCount()).isZero();
        assertThat(server.getMaxSessions()).isEqualTo(64);
        assertThat(server.getLastHeartbeatAt()).isNotNull();
        verify(serverRepository).save(server);
    }
}
