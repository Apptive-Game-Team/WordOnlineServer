package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.PingChecker;
import com.wordonline.server.game.service.CardDeck;
import com.wordonline.server.game.service.GameLoop;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Getter
// this class is used to store the session information
// it sends the frame information to the client
public class SessionObject {
    private final String sessionId;
    private final long leftUserId;
    private final long rightUserId;
    private final SimpMessagingTemplate template;
    private final String url;
    private final CardDeck leftUserCardDeck;
    private final CardDeck rightUserCardDeck;
    private final PingChecker pingChecker;

    public Master getUserSide(long userId) {
        if (userId == leftUserId) {
            return Master.LeftPlayer;
        } else if (userId == rightUserId) {
            return Master.RightPlayer;
        } else {
            return null;
        }
    }

    @Setter
    private GameLoop gameLoop;

    public SessionObject(String sessionId, long leftUserId, long rightUserId, SimpMessagingTemplate template, List<CardType> leftUserCards, List<CardType> rightUserCards){
        this.sessionId = sessionId; this.leftUserId = leftUserId; this.rightUserId = rightUserId; this.template = template;
        url = String.format("/game/%s/frameInfos", sessionId);
        leftUserCardDeck = new CardDeck(leftUserCards);
        rightUserCardDeck = new CardDeck(rightUserCards);
        pingChecker = new PingChecker(leftUserId, rightUserId,
            userId -> {
                Master loser = getUserSide(userId);
                gameLoop.resultChecker.setLoser(loser);
            }
        );
    }

    // this method is used to send the frame information to the client
    public void sendFrameInfo(long userId, Object data){
        template.convertAndSend(String.format("%s/%d", url, userId), data);
    }

    @Override
    public String toString() {
        double fps = gameLoop.deltaTime > 0 ? 1 / gameLoop.deltaTime : 0.0;
        return String.format("Session(users: [%d, %d], isRunning: %s, currentFps: %.2f)",
                leftUserId,
                rightUserId,
                gameLoop.is_running(),
                fps);
    }
}

