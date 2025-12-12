package com.wordonline.server.server.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.wordonline.server.server.entity.ServerState;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerStatusTracker {

    private final ServerStatusService serverStatusService;

    @PostConstruct
    public void onStart() {
        serverStatusService.setServerStatus(ServerState.ACTIVE);
    }

    @PreDestroy
    public void onDestroy() {
        serverStatusService.setServerStatus(ServerState.INACTIVE);
    }
}
