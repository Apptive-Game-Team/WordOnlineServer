package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.PingChecker;
import com.wordonline.server.game.service.CardDeck;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import javax.smartcardio.Card;

@Getter
// this class is used to store the session information
// it sends the frame information to the client
public class SessionObject {
    private final String sessionId;
    private long leftUserId;
    private long rightUserId;
    private final SimpMessagingTemplate template;
    private final String url;
    private final CardDeck leftUserCardDeck;
    private final CardDeck rightUserCardDeck;
    private final PingChecker pingChecker;
    private final SessionType sessionType;

    public Master getUserSide(long userId) {
        if (userId == leftUserId) {
            return Master.LeftPlayer;
        } else if (userId == rightUserId) {
            return Master.RightPlayer;
        } else {
            return null;
        }
    }

    public boolean isLeftBot() {
        return leftUserId < 0;
    }

    public boolean isRightBot() {
        return rightUserId < 0;
    }

    @Setter
    private GameLoop gameLoop;

    public GameContext getGameContext() {
        return gameLoop.getGameContext();
    }

    public SessionObject(String sessionId, long leftUserId, long rightUserId, SimpMessagingTemplate template, List<CardType> leftUserCards, List<CardType> rightUserCards, SessionType sessionType){
        this.sessionId = sessionId; this.leftUserId = leftUserId; this.rightUserId = rightUserId; this.template = template;
        url = String.format("/game/%s/frameInfos", sessionId);
        leftUserCardDeck = new CardDeck(leftUserCards);
        rightUserCardDeck = new CardDeck(rightUserCards);
        pingChecker = new PingChecker(leftUserId, rightUserId,
            userId -> {
                Master loser = getUserSide(userId);
                getGameContext().getResultChecker().setLoser(loser);
            }
        );
        this.sessionType = sessionType;
    }
    public SessionObject(String sessionId, long leftUserId, long rightUserId, SimpMessagingTemplate template, List<CardType> leftUserCards, List<CardType> rightUserCards) {
         this(sessionId, leftUserId, rightUserId, template, leftUserCards, rightUserCards, SessionType.PVP);
    }

    // this method is used to send the frame information to the client
    public void sendFrameInfo(long userId, Object data){
        template.convertAndSend(String.format("%s/%d", url, userId), data);
    }

    public void setLeftUser(long userId, List<CardType> cards) {
        leftUserId = userId;
        getGameContext().getGameSessionData().leftPlayerData.cards.clear();
        getGameContext().getGameSessionData().leftCardDeck.setCards(cards);
    }

    public void setRightUser(long userId, List<CardType> cards) {
        rightUserId = userId;
        getGameContext().getGameSessionData().rightPlayerData.cards.clear();
        getGameContext().getGameSessionData().rightCardDeck.setCards(cards);
    }

    @Override
    public String toString() {
        float deltaTime = gameLoop.getGameContext().getDeltaTime();
        double fps = deltaTime > 0 ? 1 / deltaTime : 0.0;
        return String.format("Session(users: [%d, %d], isRunning: %s, currentFps: %.2f)",
                leftUserId,
                rightUserId,
                gameLoop.is_running(),
                fps);
    }
}

