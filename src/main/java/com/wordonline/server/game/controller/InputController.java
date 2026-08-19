package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.session.service.SessionService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.input.InputResponseDto;
import com.wordonline.server.game.dto.input.MagicUseRequestDto;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.service.LocalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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

        if (sessionObject.getUserSide(userId) == null) {
            throw new AuthorizationDeniedException(localizationService.getMessage("error.authorization.denied"));
        }

        log.trace("input arrived {}", inputRequestDto.getType());

        // This runs on a STOMP inbound thread. Anything that touches game state is queued for the
        // loop thread; the request payload is converted here so a malformed one is rejected with an
        // exception on this thread rather than logged out of a queued action.
        GameContext gameContext = sessionObject.getGameContext();

        switch (inputRequestDto.getType()) {
            case "useMagic" -> {
                log.trace("useMagic arrived {}", userId);
                MagicUseRequestDto magicUse = inputRequestDto.toMagicUse();
                gameContext.submitAction("useMagic", () -> {
                    InputResponseDto responseDto = gameContext.getMagicInputHandler()
                            .handleInput(gameContext, userId, magicUse);
                    sessionObject.sendFrameInfo(userId, responseDto);
                });
            }
            case "ping" -> {
                log.trace("ping arrived {}", userId);
                sessionObject.getPingChecker().ping(userId);
            }
            case "selectCard" -> {
                log.trace("selectCard arrived {}", userId);
                CardType card = inputRequestDto.toCardSelect().card();
                gameContext.submitAction("selectCard", () -> gameContext.selectCard(userId, card));
            }
            case "unselectCard" -> {
                log.trace("unselectCard arrived {}", userId);
                CardType card = inputRequestDto.toCardUnselect().card();
                gameContext.submitAction("unselectCard", () -> gameContext.unselectCard(userId, card));
            }
            case "cancelCard" -> {
                log.trace("cancelCard arrived {}", userId);
                inputRequestDto.toCardCancel();
                gameContext.submitAction("cancelCard", () -> gameContext.unselectAllCard(userId));
            }
            case null, default -> log.warn("Unknown input type: {}", inputRequestDto.getType());
        }

    }
}
