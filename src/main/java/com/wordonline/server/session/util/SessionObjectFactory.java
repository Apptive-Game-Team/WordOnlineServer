package com.wordonline.server.session.util;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.session.dto.SessionDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionObjectFactory {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final DeckService deckService;

    public SessionObject createSessionObject(SessionDto sessionDto) {
        long leftUserId = sessionDto.uid1();
        long rightUserId = sessionDto.uid2();

        if (rightUserId == -1) {
            return createPracticeSessionObject(sessionDto);
        }

        return new SessionObject(
                sessionDto.sessionId(),
                leftUserId,
                rightUserId,
                simpMessagingTemplate,
                deckService.getSelectedCards(leftUserId),
                deckService.getSelectedCards(rightUserId)
        );
    }

    private SessionObject createPracticeSessionObject(SessionDto sessionDto) {
        long leftUserId = sessionDto.uid1();
        long rightUserId = sessionDto.uid2();

        return new SessionObject(
                sessionDto.sessionId(),
                leftUserId,
                rightUserId,
                simpMessagingTemplate,
                deckService.getSelectedCards(leftUserId),
                deckService.getSelectedCards(leftUserId),
                SessionType.Practice
        );
    }
}
