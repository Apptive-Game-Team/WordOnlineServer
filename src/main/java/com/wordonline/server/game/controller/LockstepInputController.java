package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.lockstep.FrameSubmissionDto;
import com.wordonline.server.game.dto.lockstep.ClientReadyDto;
import com.wordonline.server.game.service.InputRelayLoop;
import com.wordonline.server.service.LocalizationService;
import com.wordonline.server.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@MessageMapping("/game/lockstep")
public class LockstepInputController {
    private final SessionService sessionService;
    private final LocalizationService localizationService;

    @MessageMapping("input/{sessionId}/{userId}")
    public void submit(@DestinationVariable String sessionId,
                       @DestinationVariable long userId,
                       @Payload FrameSubmissionDto submission,
                       @AuthenticationPrincipal PrincipalDetails principalDetails) {
        InputRelayLoop relayLoop = authorizedRelay(sessionId, userId, principalDetails);
        if (relayLoop != null) {
            relayLoop.submit(userId, submission);
        }
    }

    @MessageMapping("ready/{sessionId}/{userId}")
    public void ready(@DestinationVariable String sessionId,
                      @DestinationVariable long userId,
                      @Payload ClientReadyDto ready,
                      @AuthenticationPrincipal PrincipalDetails principalDetails) {
        InputRelayLoop relayLoop = authorizedRelay(sessionId, userId, principalDetails);
        if (relayLoop != null) {
            relayLoop.ready(userId, ready);
        }
    }

    private InputRelayLoop authorizedRelay(String sessionId, long userId, PrincipalDetails principalDetails) {
        if (principalDetails == null || userId != principalDetails.getUid()) {
            throw new AuthorizationDeniedException(localizationService.getMessage("error.authorization.denied"));
        }
        SessionObject session = sessionService.getSessionObject(sessionId);
        if (session == null) {
            return null;
        }
        if (!(session.getGameLoop() instanceof InputRelayLoop relayLoop)) {
            throw new IllegalStateException("Session does not use lockstep protocol");
        }
        return relayLoop;
    }
}
