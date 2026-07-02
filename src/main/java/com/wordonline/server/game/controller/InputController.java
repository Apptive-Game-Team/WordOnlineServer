package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.session.service.SessionService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.input.InputResponseDto;
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
    public void handleInput(
            @DestinationVariable String sessionId,
            @DestinationVariable long userId,
            @Payload InputRequestDto inputRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (userId != principalDetails.getUid()) {
            throw new AuthorizationDeniedException(localizationService.getMessage("error.authorization.denied"));
        }

        SessionObject sessionObject = sessionService.getSessionObject(sessionId);

        if (sessionObject == null) return;

        log.trace("input arrived {}", inputRequestDto.getType());

        switch (inputRequestDto.getType()) {
            case "useMagic" -> {
                log.trace("useMagic arrived {}", userId);
                InputResponseDto responseDto = sessionObject.getGameContext().getMagicInputHandler().handleInput(
                        sessionObject.getGameContext(), userId, inputRequestDto.toMagicUse()
                );
                template.convertAndSend(String.format("/game/%s/frameInfos/%s", sessionId, userId), responseDto);
            }
            case "ping" -> {
                log.trace("ping arrived {}", userId);
                sessionObject.getPingChecker().ping(userId);
            }
            case "selectCard" -> {
                log.trace("selectCard arrived {}", userId);
                sessionObject.getGameContext().selectCard(userId, inputRequestDto.toCardSelect().card());
            }
            case "unselectCard" -> {
                log.trace("unselectCard arrived {}", userId);
                sessionObject.getGameContext().unselectCard(userId, inputRequestDto.toCardUnselect().card());
            }
            case "cancelCard" -> {
                log.trace("cancelCard arrived {}", userId);
                inputRequestDto.toCardCancel();
                sessionObject.getGameContext().unselectAllCard(userId);
            }
            default -> log.warn("Unknown input type: {}", inputRequestDto.getType());
        }

    }
}
