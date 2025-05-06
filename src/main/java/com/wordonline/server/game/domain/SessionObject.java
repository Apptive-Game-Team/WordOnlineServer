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
    final String url;

    @Setter
    private GameLoop gameLoop;

    public SessionObject(String sessionId, String leftUserId, String rightUserId, SimpMessagingTemplate template){
        this.sessionId = sessionId; this.leftUserId = leftUserId; this.rightUserId = rightUserId; this.template = template;
        url = String.format("/game/%s/frameInfos", sessionId);
    }

    public void sendFrameInfo(String userId, Object data){
        template.convertAndSend(String.format("%s/%s", url, userId), data);
    }
}

