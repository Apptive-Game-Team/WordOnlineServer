package com.wordonline.server.session.util;

import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.RunType;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.session.dto.SessionDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SessionObjectFactory {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final DeckService deckService;

    public SessionObjectFactory(SimpMessagingTemplate simpMessagingTemplate, DeckService deckService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.deckService = deckService;
    }

    public SessionObject createSessionObject(SessionDto sessionDto) {
        Long uid1 = sessionDto.uid1();
        Long uid2 = sessionDto.uid2();
        String sessionId = sessionDto.sessionId();

        SessionType sessionType = resolveSessionType(sessionDto, sessionId, uid1, uid2);

        return switch (sessionType) {
            case PVE -> createPveSessionObject(sessionId, uid1, sessionDto.scenarioId(), sessionDto.runType(), sessionDto.parameterProfileId(), sessionDto.simulationBatchId());
            case Practice -> createPracticeSessionObject(sessionId, uid1, uid2, sessionDto.runType(), sessionDto.parameterProfileId(), sessionDto.simulationBatchId());
            case PVP -> createPvpSessionObject(sessionId, uid1, uid2, sessionDto.runType(), sessionDto.parameterProfileId(), sessionDto.simulationBatchId());
        };
    }

    private SessionType resolveSessionType(SessionDto sessionDto, String sessionId, Long uid1, Long uid2) {
        SessionType sessionType = sessionDto.sessionType();
        if (sessionType != null) {
            return sessionType;
        }

        boolean isPve = sessionId != null && sessionId.toLowerCase().contains("pve");
        if (isPve) {
            return SessionType.PVE;
        }

        if (uid1 < 0 || uid2 < 0) {
            return SessionType.Practice;
        }

        return SessionType.PVP;
    }

    private SessionObject createPvpSessionObject(String sessionId,
                                                 long uid1,
                                                 long uid2,
                                                 RunType runType,
                                                 Long parameterProfileId,
                                                 UUID simulationBatchId) {
        List<CardType> leftCards = deckService.getSelectedCards(uid1);
        List<CardType> rightCards = deckService.getSelectedCards(uid2);
        return new SessionObject(sessionId, uid1, uid2, simpMessagingTemplate, leftCards, rightCards, SessionType.PVP, null, runType, parameterProfileId, simulationBatchId);
    }

    private SessionObject createPracticeSessionObject(String sessionId,
                                                      long uid1,
                                                      long uid2,
                                                      RunType runType,
                                                      Long parameterProfileId,
                                                      UUID simulationBatchId) {
        List<CardType> leftCards = deckService.getSelectedCards(uid1);
        List<CardType> rightCards = deckService.getSelectedCards(uid2);
        return new SessionObject(sessionId, uid1, uid2, simpMessagingTemplate, leftCards, rightCards, SessionType.Practice, null, runType, parameterProfileId, simulationBatchId);
    }

    private SessionObject createPveSessionObject(String sessionId,
                                                 long uid1,
                                                 Long scenarioId,
                                                 RunType runType,
                                                 Long parameterProfileId,
                                                 UUID simulationBatchId) {
        List<CardType> leftCards = uid1 >= 0 ? deckService.getSelectedCards(uid1) : List.of();
        return new SessionObject(sessionId, uid1, -1, simpMessagingTemplate, leftCards, List.of(), SessionType.PVE, scenarioId, runType, parameterProfileId, simulationBatchId);
    }
}
