package com.wordonline.server.game.controller;

import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.MmrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;

// PracticeController.java
@Slf4j
@Controller
@RequiredArgsConstructor
public class PracticeController {

    private final DeckService deckService;
    private final SimpMessagingTemplate template;
    private final SessionManager sessionManager;
    private final UserService userService;
    private final Parameters parameters;

    public record PracticeStartDto(String botType /*"HEURISTIC"*/, Long seed) {}
    public record PracticeStartedDto(String sessionId, String frameTopic /* /game/{id}/frameInfos/{userId} */) {}

    @MessageMapping("/game/practice")
    public PracticeStartedDto start(PracticeStartDto dto, Principal who) {
        long uid = Long.parseLong(who.getName());

        String sessionId = "practice-" + System.nanoTime();
        var userCards = deckService.getSelectedCards(uid);
        var botCards  = userCards; // 또는 별도 봇 전용 덱 로직

        // ✅ 봇 주입
        //TrainingBot bot = new HeuristicBot();
        //gameLoop.setBot(bot);

        sessionManager.createSession(
                new SessionObject(
                        sessionId,
                        uid, -1,
                        template,
                        userCards,
                        botCards
                        ));

        String frameTopic = String.format("/game/%s/frameInfos/%d", sessionId, uid);
        return new PracticeStartedDto(sessionId, frameTopic);
    }
}
