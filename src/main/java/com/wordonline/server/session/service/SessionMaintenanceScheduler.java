package com.wordonline.server.session.service;

import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.service.ServerStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionMaintenanceScheduler {

    private static final long BOT_VS_BOT_MAINTENANCE_INTERVAL = 10_000L;

    private final SessionService sessionService;
    private final ServerStatusService serverStatusService;

    @Scheduled(fixedDelay = BOT_VS_BOT_MAINTENANCE_INTERVAL)
    public void maintainBotVsBotSession() {
        if (!serverStatusService.getCurrentState().equals(ServerState.ACTIVE)) {
            return;
        }

        sessionService.createBotVsBotSessionIfBelowThreshold();
    }
}
