package com.wordonline.server.debug.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.wordonline.server.debug.dto.DebugGameRequestDto;
import com.wordonline.server.debug.dto.DebugGameResponseDto;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebugService {

    private static final AtomicInteger sessionIdCounter = new AtomicInteger(1);
    private static final String DEBUG_SESSION_PREFIX = "debug-";
    private static final String DEBUG_PVE_SESSION_PREFIX = "debug-pve-";

    private final SessionService sessionService;
    private final DeckService deckService;

    private SessionObject debugSession;

    public DebugGameResponseDto enterPracticeSession(DebugGameRequestDto debugGameRequestDto) {
        DebugGameResponseDto dto = new DebugGameResponseDto(createPveDebugSession(debugGameRequestDto.userId(), -1));
        log.info("Entering practice session userId: {}", debugGameRequestDto.userId());
        return dto;
    }

    public DebugGameResponseDto enterTestSession(DebugGameRequestDto debugGameRequestDto) {
        DebugGameResponseDto dto = new DebugGameResponseDto(getSessionId());
        log.info("Entering test session userId: {}, side: {}", debugGameRequestDto.userId(), debugGameRequestDto.side());
        long userId = debugGameRequestDto.userId();
        if (debugGameRequestDto.side() == Master.LeftPlayer) {
            log.info("left");
            debugSession.setLeftUser(
                    userId,
                    deckService.getSelectedCards(userId)
            );
        } else if (debugGameRequestDto.side() == Master.RightPlayer) {
            log.info("right");
            debugSession.setRightUser(
                    userId,
                    deckService.getSelectedCards(userId)
            );
        }
        return dto;
    }

    private String getSessionId() {
        if (isActive(debugSession)) {
            return debugSession.getSessionId();
        }

        return createDebugSession();
    }

    private String createDebugSession() {
        return createDebugSession(0, 0);
    }

    private String createDebugSession(long uid1, long uid2) {
        return createDebugSession(DEBUG_SESSION_PREFIX, uid1, uid2, SessionType.PVP, null);
    }

    private String createPveDebugSession(long uid1, long uid2) {
        return createDebugSession(DEBUG_PVE_SESSION_PREFIX, uid1, uid2, SessionType.Practice, null);
    }

    private String createDebugSession(String sessionPrefix, long uid1, long uid2, SessionType sessionType, Long scenarioId) {
        SessionDto sessionDto = new SessionDto(
                sessionPrefix + sessionIdCounter.getAndIncrement(),
                uid1,
                uid2,
                sessionType,
                scenarioId
        );
        sessionService.createSession(sessionDto);
        debugSession = sessionService.getSessionObject(sessionDto.sessionId());
        return sessionDto.sessionId();
    }

    private boolean isActive(SessionObject debugSession) {
        if (debugSession == null) {
            return false;
        }
        return debugSession.getGameLoop().is_running();
    }
}