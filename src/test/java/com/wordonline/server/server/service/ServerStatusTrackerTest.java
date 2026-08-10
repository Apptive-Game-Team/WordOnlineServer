package com.wordonline.server.server.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ConfigurableApplicationContext;

import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.session.service.SessionService;

@ExtendWith(MockitoExtension.class)
class ServerStatusTrackerTest {

    @Mock
    private ConfigurableApplicationContext context;

    @Mock
    private ServerStatusService serverStatusService;

    @Mock
    private SessionService sessionService;

    private ServerStatusTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new ServerStatusTracker(context, serverStatusService, sessionService);
    }

    @Test
    void startPublishesActiveStateAndInitialHeartbeat() {
        when(sessionService.getActiveSessions()).thenReturn(3L);

        tracker.onStart();

        verify(serverStatusService).setServerStatus(ServerState.ACTIVE);
        verify(sessionService).subscribeSessionNumChange(any());
        verify(serverStatusService).publishHeartbeat(3);
    }

    @Test
    void scheduledHeartbeatPublishesCurrentSessionCount() {
        when(sessionService.getActiveSessions()).thenReturn(4L);

        tracker.heartbeat();

        verify(serverStatusService).publishHeartbeat(4);
    }

    @Test
    void heartbeatFailureIsIsolated() {
        when(sessionService.getActiveSessions()).thenReturn(4L);
        doThrow(new IllegalStateException("database unavailable"))
                .when(serverStatusService).publishHeartbeat(4);

        tracker.heartbeat();

        verify(serverStatusService).publishHeartbeat(4);
        verify(serverStatusService, never()).setServerStatus(ServerState.INACTIVE);
    }

    @Test
    void destroyPublishesInactiveState() {
        tracker.onDestroy();

        verify(serverStatusService).setServerStatus(ServerState.INACTIVE);
    }
}
