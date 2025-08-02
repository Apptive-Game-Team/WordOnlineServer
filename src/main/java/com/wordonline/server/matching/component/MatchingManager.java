package com.wordonline.server.matching.component;

import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.matching.dto.MatchedInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingManager {

    private final Queue<Long> matchingQueue = new LinkedList<>();
    private static int sessionIdCounter = 0;

    private final DeckService deckService;
    private final UserService userService;
    private final SimpMessagingTemplate template;
    private final SessionManager sessionManager;

    public boolean enqueue(long userId) {
        if (deckService.hasSelectedDeck(userId)) {
            matchingQueue.add(userId);
            return true;
        }
        return false;
    }

    public boolean tryMatchUsers() {
        if (matchingQueue.size() < 2)
            return false;

        sessionIdCounter++;

        long uid1 = matchingQueue.poll();
        UserResponseDto user1 = userService.getUser(uid1);
        long uid2 = matchingQueue.poll();
        UserResponseDto user2 = userService.getUser(uid2);
        String sessionId = "session-" + sessionIdCounter;
        MatchedInfoDto matchedInfoDto = new MatchedInfoDto(
                "Successfully Matched",
                user1,
                user2,
                sessionId
        );
        template.convertAndSend(
                String.format("/queue/match-status/%s", uid1),
                matchedInfoDto);
        template.convertAndSend(
                String.format("/queue/match-status/%s", uid2),
                matchedInfoDto);

        log.info("[Matched] Users matched; user1: {}, user2: {}", uid1, uid2);

        try {
            Thread.sleep(2000);
            sessionManager.createSession(
                    new SessionObject(
                            sessionId,
                            uid1, uid2,
                            template,
                            deckService.getSelectedCards(uid1),
                            deckService.getSelectedCards(uid2)));
            return true;
        } catch (InterruptedException e) {
            log.error("Error while creating session", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public String getHealthLog() {
        return "Matching Queue: " + matchingQueue;
    }
}
