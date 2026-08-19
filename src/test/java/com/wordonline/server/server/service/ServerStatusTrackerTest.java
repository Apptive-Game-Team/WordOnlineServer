package com.wordonline.server.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    void sessionCountChangesDoNotWriteTheServerRow() {
        // The scheduled heartbeat owns session_count now. Writing it per change cost two
        // connection acquisitions on every session start, end and reap.
        when(sessionService.getActiveSessions()).thenReturn(0L);
        tracker.onStart();
        Subscriber<Integer> subscriber = subscriber();
        org.mockito.Mockito.clearInvocations(serverStatusService);

        subscriber.onNext(7);
        subscriber.onNext(6);

        verify(serverStatusService, never()).publishHeartbeat(anyInt());
    }

    @Test
    void reachingZeroSessionsStillRunsTheDrainCheck() {
        when(sessionService.getActiveSessions()).thenReturn(0L);
        when(serverStatusService.getCurrentState()).thenReturn(ServerState.ACTIVE);
        tracker.onStart();
        Subscriber<Integer> subscriber = subscriber();

        subscriber.onNext(0);

        verify(serverStatusService).getCurrentState();
    }

    @Test
    void everySessionCountChangeRequestsTheNextEvent() {
        when(sessionService.getActiveSessions()).thenReturn(0L);
        tracker.onStart();
        Subscriber<Integer> subscriber = subscriber();
        AtomicLong requested = new AtomicLong();
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                requested.addAndGet(n);
            }

            @Override
            public void cancel() {
            }
        });

        subscriber.onNext(3);

        assertThat(requested.get()).isEqualTo(2L);
    }

    @SuppressWarnings("unchecked")
    private Subscriber<Integer> subscriber() {
        ArgumentCaptor<Subscriber<Integer>> captor = ArgumentCaptor.forClass(Subscriber.class);
        verify(sessionService).subscribeSessionNumChange(captor.capture());
        Subscriber<Integer> subscriber = captor.getValue();
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
            }

            @Override
            public void cancel() {
            }
        });
        return subscriber;
    }

    @Test
    void destroyPublishesInactiveState() {
        tracker.onDestroy();

        verify(serverStatusService).setServerStatus(ServerState.INACTIVE);
    }
}
