package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.MmrService;
import com.wordonline.server.matching.component.MatchingManager;
import com.wordonline.server.matching.dto.SimpleMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;

// PracticeController.java
@Slf4j
@Controller
@RequiredArgsConstructor
public class PracticeController {

    private final MatchingManager matchingManager;
    private final SimpMessagingTemplate template;

    // matching queue request
    @MessageMapping("/game/practice")
    public void queueMatching(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        matchingManager.matchPractice(principalDetails.memberId);
        }
}
