package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.session.service.SessionService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.InputResponseDto;
import com.wordonline.server.service.LocalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@MessageMapping("/game/input")
public class InputController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private LocalizationService localizationService;

    @MessageMapping("{sessionId}/{userId}")
    public void handleInput(@DestinationVariable String sessionId, @DestinationVariable long userId, @Payload InputRequestDto inputRequestDto, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (userId != principalDetails.getUid()) {
            throw new AuthorizationDeniedException(localizationService.getMessage("error.authorization.denied"));
        }

        SessionObject sessionObject = sessionService.getSessionObject(sessionId);
        log.trace("input arrived {}", inputRequestDto.getType());

        if (sessionObject != null && inputRequestDto.getType().equals("ping")) {
            log.trace("ping arrived {}", userId);
            sessionObject.getPingChecker().ping(userId);
            return;
        }

        InputResponseDto responseDto = sessionObject.getGameContext().getMagicInputHandler().handleInput(
                sessionObject.getGameContext(), userId, inputRequestDto
        );
        template.convertAndSend(String.format("/game/%s/frameInfos/%s", sessionId, userId), responseDto);
    }
}
