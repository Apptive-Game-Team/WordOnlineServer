package com.wordonline.server.session.util;

import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.session.dto.SessionDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionObjectFactory {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final DeckService deckService;

    public SessionObjectFactory(SimpMessagingTemplate simpMessagingTemplate, DeckService deckService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.deckService = deckService;
    }

    public SessionObject createSessionObject(SessionDto sessionDto) {
        long uid1 = sessionDto.uid1().longValue();
        long uid2 = sessionDto.uid2().longValue();

        String sessionId = sessionDto.sessionId();
        boolean isPve = sessionId != null && sessionId.toLowerCase().contains("pve");

        if (isPve) {
            return createPveSessionObject(sessionId, uid1, uid2);
        }

        if (uid1 < 0 || uid2 < 0) {
            return createPracticeSessionObject(sessionId, uid1, uid2);
        }

        List<CardType> leftCards = deckService.getSelectedCards(uid1);
        List<CardType> rightCards = deckService.getSelectedCards(uid2);
        return new SessionObject(sessionId, uid1, uid2, simpMessagingTemplate, leftCards, rightCards, SessionType.PVP);
    }

    private SessionObject createPracticeSessionObject(String sessionId, long uid1, long uid2) {
        List<CardType> leftCards = uid1 >= 0 ? deckService.getSelectedCards(uid1) : List.of();
        List<CardType> rightCards = uid2 >= 0 ? deckService.getSelectedCards(uid2) : List.of();
        return new SessionObject(sessionId, uid1, uid2, simpMessagingTemplate, leftCards, rightCards, SessionType.Practice);
    }

    private SessionObject createPveSessionObject(String sessionId, long uid1, long uid2) {
        List<CardType> leftCards = uid1 >= 0 ? deckService.getSelectedCards(uid1) : List.of();
        List<CardType> rightCards = uid2 >= 0 ? deckService.getSelectedCards(uid2) : List.of();
        return new SessionObject(sessionId, uid1, uid2, simpMessagingTemplate, leftCards, rightCards, SessionType.PVE);
    }
}
