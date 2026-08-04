package com.wordonline.server.bot.service;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.service.ServerStatusService;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BotGameSchedulerTest {

    private final SessionService sessionService = mock(SessionService.class);
    private final BotPersonaService botPersonaService = mock(BotPersonaService.class);
    private final ServerStatusService serverStatusService = mock(ServerStatusService.class);
    private final BotGameScheduler scheduler = new BotGameScheduler(
            sessionService,
            botPersonaService,
            serverStatusService
    );

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "enabled", true);
        when(serverStatusService.getCurrentState()).thenReturn(ServerState.ACTIVE);
    }

    @Test
    void createsPracticeSessionWithEnabledBotsWhenIdle() {
        BotPersona beginner = bot(-1, "Beginner Bot");
        BotPersona advanced = bot(-2, "Advanced Bot");
        when(sessionService.getActiveSessions()).thenReturn(0L);
        when(botPersonaService.findEnabled()).thenReturn(List.of(beginner, advanced));

        scheduler.ensureBotGameWhenIdle();

        ArgumentCaptor<SessionDto> captor = ArgumentCaptor.forClass(SessionDto.class);
        verify(sessionService).createSession(captor.capture());
        SessionDto sessionDto = captor.getValue();

        assertThat(sessionDto.sessionId()).startsWith("bot-auto-");
        assertThat(Set.of(sessionDto.uid1(), sessionDto.uid2()))
                .containsExactlyInAnyOrder(
                        beginner.userId(),
                        advanced.userId()
                );
        assertThat(sessionDto.uid1()).isNotEqualTo(sessionDto.uid2());
        assertThat(sessionDto.sessionType()).isEqualTo(SessionType.Practice);
        assertThat(sessionDto.scenarioId()).isNull();
    }

    @Test
    void skipsWhenOnlyOneEnabledBotPersonaExists() {
        BotPersona beginner = bot(-1, "Beginner Bot");
        when(sessionService.getActiveSessions()).thenReturn(0L);
        when(botPersonaService.findEnabled()).thenReturn(List.of(beginner));

        scheduler.ensureBotGameWhenIdle();

        verify(sessionService, never()).createSession(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsWhenAnySessionIsActive() {
        when(sessionService.getActiveSessions()).thenReturn(1L);

        scheduler.ensureBotGameWhenIdle();

        verify(sessionService, never()).createSession(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsWhenNoEnabledBotsExist() {
        when(sessionService.getActiveSessions()).thenReturn(0L);
        when(botPersonaService.findEnabled()).thenReturn(List.of());

        scheduler.ensureBotGameWhenIdle();

        verify(sessionService, never()).createSession(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsWhenServerIsNotActive() {
        when(serverStatusService.getCurrentState()).thenReturn(ServerState.INACTIVE);

        scheduler.ensureBotGameWhenIdle();

        verify(sessionService, never()).createSession(org.mockito.ArgumentMatchers.any());
    }

    private BotPersona bot(long id, String name) {
        return new BotPersona(
                id,
                name,
                BotTier.BEGINNER,
                250,
                8,
                0.25,
                true
        );
    }
}
