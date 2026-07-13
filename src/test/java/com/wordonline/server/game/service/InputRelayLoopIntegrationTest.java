package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.lockstep.ClientReadyDto;
import com.wordonline.server.game.dto.lockstep.ConfirmedFrameDto;
import com.wordonline.server.game.dto.lockstep.FrameSubmissionDto;
import com.wordonline.server.game.dto.lockstep.LockstepAbortDto;
import com.wordonline.server.game.dto.lockstep.LockstepAbortReason;
import com.wordonline.server.game.dto.lockstep.LockstepSessionStartDto;
import com.wordonline.server.game.service.lockstep.LockstepMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InputRelayLoopIntegrationTest {
    private SessionObject session;
    private InputRelayLoop loop;
    private LockstepMetrics metrics;

    @BeforeEach
    void setUp() {
        GameContext context = mock(GameContext.class);
        session = mock(SessionObject.class);
        CardDeck leftDeck = mock(CardDeck.class);
        CardDeck rightDeck = mock(CardDeck.class);
        when(session.getSessionId()).thenReturn("session-1");
        when(session.getLeftUserId()).thenReturn(10L);
        when(session.getRightUserId()).thenReturn(20L);
        when(session.getSessionType()).thenReturn(SessionType.PVP);
        when(session.getRandomSeed()).thenReturn(42L);
        when(session.getLeftUserCardDeck()).thenReturn(leftDeck);
        when(session.getRightUserCardDeck()).thenReturn(rightDeck);
        when(leftDeck.snapshot()).thenReturn(List.of());
        when(rightDeck.snapshot()).thenReturn(List.of());
        metrics = new LockstepMetrics();
        loop = new InputRelayLoop(mock(MmrService.class), mock(UserService.class), context,
                mock(Parameters.class), 1, "sim-1", "config-1", 50, 50, 2, metrics);
        loop.init(session, () -> { });
    }

    @Test
    void relaysConfirmedFrameToTwoPlayersAndSpectatorAfterReadyBarrier() {
        readyBoth();
        loop.submit(10L, new FrameSubmissionDto(1, 1, "same", List.of()));
        loop.submit(20L, new FrameSubmissionDto(1, 1, "same", List.of()));

        loop.update();

        ArgumentCaptor<Object> broadcast = ArgumentCaptor.forClass(Object.class);
        verify(session, org.mockito.Mockito.times(2)).broadcastFrameInfo(broadcast.capture());
        assertThat(broadcast.getAllValues()).anyMatch(LockstepSessionStartDto.class::isInstance);
        assertThat(broadcast.getAllValues()).anyMatch(ConfirmedFrameDto.class::isInstance);
        LockstepSessionStartDto start = broadcast.getAllValues().stream()
                .filter(LockstepSessionStartDto.class::isInstance)
                .map(LockstepSessionStartDto.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(start.bootstrapEvents()).extracting(event -> event.sequence()).containsExactly(0, 1);
        verify(session, org.mockito.Mockito.times(2)).sendFrameInfo(org.mockito.ArgumentMatchers.eq(10L), any());
        verify(session, org.mockito.Mockito.times(2)).sendFrameInfo(org.mockito.ArgumentMatchers.eq(20L), any());
        assertThat(metrics.snapshot().startedSessions()).isEqualTo(1);
        assertThat(metrics.snapshot().confirmedFrames()).isEqualTo(1);
    }

    @Test
    void abortsWithoutReconnectWhenParticipantDisconnects() {
        readyBoth();
        loop.submit(10L, new FrameSubmissionDto(1, 1, "same", List.of()));
        loop.submit(20L, new FrameSubmissionDto(1, 1, "same", List.of()));
        loop.update();

        loop.disconnected(20L);

        ArgumentCaptor<Object> broadcast = ArgumentCaptor.forClass(Object.class);
        verify(session, org.mockito.Mockito.times(3)).broadcastFrameInfo(broadcast.capture());
        assertThat(broadcast.getAllValues()).filteredOn(LockstepAbortDto.class::isInstance)
                .singleElement()
                .extracting(value -> ((LockstepAbortDto) value).reason())
                .isEqualTo(LockstepAbortReason.PARTICIPANT_DISCONNECTED);
        assertThat(loop.is_running()).isFalse();
    }

    @Test
    void rejectsMismatchedClientBeforeSessionStart() {
        assertThatThrownBy(() -> loop.ready(10L, new ClientReadyDto(1, "other", "config-1")))
                .isInstanceOf(IllegalArgumentException.class);

        ArgumentCaptor<Object> broadcast = ArgumentCaptor.forClass(Object.class);
        verify(session).broadcastFrameInfo(broadcast.capture());
        assertThat(broadcast.getValue()).isInstanceOf(LockstepAbortDto.class);
        assertThat(((LockstepAbortDto) broadcast.getValue()).reason())
                .isEqualTo(LockstepAbortReason.VERSION_MISMATCH);
        assertThat(metrics.snapshot().abortedSessions().get(LockstepAbortReason.VERSION_MISMATCH)).isEqualTo(1);
    }

    private void readyBoth() {
        ClientReadyDto ready = new ClientReadyDto(1, "sim-1", "config-1");
        loop.ready(10L, ready);
        loop.ready(20L, ready);
    }
}
