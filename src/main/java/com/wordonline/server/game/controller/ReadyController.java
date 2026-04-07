package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

/**
 * 클라이언트가 WebSocket 구독 완료 후 게임 세션 참여 준비가 됐음을 알리는 엔드포인트.
 * 모든 human 플레이어가 ready를 보내면 게임 루프가 시작된다.
 *
 * Destination: /app/game/ready/{sessionId}/{userId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@MessageMapping("/game/ready")
public class ReadyController {

    private final SessionService sessionService;

    @MessageMapping("{sessionId}/{userId}")
    public void handleReady(@DestinationVariable String sessionId,
                            @DestinationVariable long userId,
                            @AuthenticationPrincipal PrincipalDetails principal) {
        if (userId != principal.getUid()) {
            throw new AuthorizationDeniedException("userId mismatch");
        }

        log.debug("[Ready] sessionId={} userId={}", sessionId, userId);
        sessionService.onReady(sessionId, userId);
    }
}
