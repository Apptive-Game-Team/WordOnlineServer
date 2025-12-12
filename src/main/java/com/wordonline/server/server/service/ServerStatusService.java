package com.wordonline.server.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.wordonline.server.server.entity.Server;
import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.entity.ServerType;
import com.wordonline.server.server.repository.ServerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerStatusService {

    private final ServerRepository serverRepository;

    @Value("${server.external-port}")
    private Integer port;

    @Value("${server.domain}")
    private String domain;

    @Value("${server.protocol}")
    private String protocol;

    public void setServerStatus(ServerState state) {
        Server server = serverRepository.findByDomainAndPort(domain, port)
                .orElseGet(() -> new Server(protocol, domain, port, ServerType.GAME, state));
        server.setState(state);
        serverRepository.save(server);
    }
}
