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

    @Setter
    private GameLoop gameLoop;

    public SessionObject(String sessionId, String leftUserId, String rightUserId, SimpMessagingTemplate template){
        this.sessionId = sessionId; this.leftUserId = leftUserId; this.rightUserId = rightUserId; this.template = template;
        broadcastUrl = String.format("/game/%s/frameinfos", sessionId);
    }

    public void broadcastFrameInfo(Object data){
        template.convertAndSend(broadcastUrl, data);
    }
}

