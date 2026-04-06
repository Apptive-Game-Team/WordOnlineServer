package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

/**
 * Receives game-result reports from clients.
 *
 * Used by PVE and other single-client sessions where the server does not
 * simulate the game and cannot detect victory/defeat on its own.
 * The client sends the loser when it detects game-over via SimWorld.IsGameOver.
 *
 * Destination: /app/game/result/{sessionId}/{userId}
 * Payload: { "loser": "LeftPlayer" | "RightPlayer" | "None" }
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@MessageMapping("/game/result")
public class ResultController {

    private final SessionService sessionService;

    @MessageMapping("{sessionId}/{userId}")
    public void handleResult(@DestinationVariable String sessionId,
                             @DestinationVariable long userId,
                             @Payload ResultPayload payload,
                             @AuthenticationPrincipal PrincipalDetails principal) {
        if (userId != principal.getUid()) {
            throw new AuthorizationDeniedException("userId mismatch");
        }

        SessionObject session = sessionService.getSessionObject(sessionId);
        if (session == null) {
            log.warn("[Result] Session not found: {}", sessionId);
            return;
        }

        Master loser = parseLoser(payload.loser());
        log.info("[Result] sessionId={} userId={} reported loser={}", sessionId, userId, loser);
        session.getGameContext().getResultChecker().setLoser(loser);
    }

    private static Master parseLoser(String loser) {
        if (loser == null) return null;
        return switch (loser) {
            case "LeftPlayer"  -> Master.LeftPlayer;
            case "RightPlayer" -> Master.RightPlayer;
            default            -> null; // treat "None" / unknown as draw
        };
    }

    public record ResultPayload(String loser) {}
}
