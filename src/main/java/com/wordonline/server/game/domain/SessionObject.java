package com.wordonline.server.game.domain;

import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Getter
// this class is used to store the session information
// it sends the frame information to the client
public class SessionObject {
    final String sessionId;
    final String leftUserId;
    final String rightUserId;
    final SimpMessagingTemplate template;
    final String url;

    public Master getUserSide(String userId) {
        if (userId.equals(leftUserId)) {
            return Master.LeftPlayer;
        } else if (userId.equals(rightUserId)) {
            return Master.RightPlayer;
        } else {
            return null;
        }
    }

    @Setter
    private GameLoop gameLoop;

    public SessionObject(String sessionId, String leftUserId, String rightUserId, SimpMessagingTemplate template){
        this.sessionId = sessionId; this.leftUserId = leftUserId; this.rightUserId = rightUserId; this.template = template;
        url = String.format("/game/%s/frameInfos", sessionId);
    }

    // this method is used to send the frame information to the client
    public void sendFrameInfo(String userId, Object data){
        template.convertAndSend(String.format("%s/%s", url, userId), data);
    }
}

