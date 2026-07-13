package com.wordonline.server.websocket;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.game.service.InputRelayLoop;
import com.wordonline.server.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class LockstepDisconnectListener {
    private final SessionService sessionService;

    @EventListener
    public void disconnected(SessionDisconnectEvent event) {
        if (!(event.getUser() instanceof PrincipalDetails principal)) {
            return;
        }
        sessionService.findByUserId(principal.getUid())
                .map(session -> session.getGameLoop())
                .filter(InputRelayLoop.class::isInstance)
                .map(InputRelayLoop.class::cast)
                .ifPresent(loop -> loop.disconnected(principal.getUid()));
    }
}
