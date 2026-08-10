package com.wordonline.server.server.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.wordonline.server.server.config.ServerIdentityProperties;
import com.wordonline.server.server.entity.Server;
import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.entity.ServerType;
import com.wordonline.server.server.repository.ServerRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerStatusService {

    private final ServerRepository serverRepository;
    private final ServerIdentityProperties serverIdentityProperties;
    private final ServerInstanceIdProvider serverInstanceIdProvider;

    @Getter
    private volatile ServerState currentState = ServerState.ACTIVE;

    public synchronized void setServerStatus(ServerState state) {
        publishStatus(state, state == ServerState.INACTIVE ? 0 : null);
    }

    public synchronized void publishHeartbeat(int sessionCount) {
        publishStatus(currentState, sessionCount);
    }

    private void publishStatus(ServerState state, Integer sessionCount) {
        currentState = state;
        String protocol = serverIdentityProperties.protocol();
        String domain = serverIdentityProperties.domain();
        Integer port = serverIdentityProperties.externalPort();

        Server server = serverRepository.findByDomainAndPort(domain, port)
                .orElseGet(() -> new Server(protocol, domain, port, ServerType.GAME, state));
        server.setState(state);
        server.setLastHeartbeatAt(Instant.now());
        if (sessionCount != null) {
            server.setSessionCount(sessionCount);
        }
        server.setMaxSessions(serverIdentityProperties.maxSessions());
        // Every write, boot and heartbeat alike, so the row always names the process that is
        // actually holding the sessions. The lobby reads a stale id as "those sessions are gone".
        server.setInstanceId(serverInstanceIdProvider.getInstanceId());
        serverRepository.save(server);
    }
}
