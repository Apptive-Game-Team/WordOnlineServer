package com.wordonline.server.game.domain;

import com.wordonline.server.game.service.GameLoop;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Getter
public class SessionObject {
    final String sessionId;
    final String leftUserId;
    final String rightUserId;
    final SimpMessagingTemplate template;
    final String broadcastUrl;

    public SessionObject(String sessionId, String leftUserId, String rightUserId, SimpMessagingTemplate template){
        this.sessionId = sessionId; this.leftUserId = leftUserId; this.rightUserId = rightUserId; this.template = template;
        broadcastUrl = String.format("/game/%s/frameinfos", sessionId);
    }

    @Setter
    GameLoop gameLoop;

    public void broadcastFrameInfo(FrameInfo frameInfo){
        template.convertAndSend(broadcastUrl, frameInfo);
    }
}

