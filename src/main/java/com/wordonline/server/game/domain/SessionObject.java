package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.PingChecker;
import com.wordonline.server.game.service.CardDeck;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.WordOnlineLoop;
import com.wordonline.server.game.util.DeckSeedDeriver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

@Getter
@Slf4j
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
    private final Long scenarioId;
    private final long randomSeed;
    // Session ids are random UUIDs, so a room list ordered by id says nothing about age. The admin
    // page needs this to tell a session created seconds ago from one that has been running for a while.
    private final Instant createdAt = Instant.now();

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

    public SessionObject(String sessionId,
                         long leftUserId,
                         long rightUserId,
                         SimpMessagingTemplate template,
                         List<CardType> leftUserCards,
                         List<CardType> rightUserCards,
                         SessionType sessionType,
                         Long scenarioId) {
        this.sessionId = sessionId;
        this.leftUserId = leftUserId;
        this.rightUserId = rightUserId;
        this.template = template;
        this.url = String.format("/game/%s/frameInfos", sessionId);
        this.randomSeed = ThreadLocalRandom.current().nextLong();
        long leftDeckSeed = DeckSeedDeriver.forLeftDeck(randomSeed);
        long rightDeckSeed = DeckSeedDeriver.forRightDeck(randomSeed);
        this.leftUserCardDeck = new CardDeck(leftUserCards, leftDeckSeed);
        this.rightUserCardDeck = new CardDeck(rightUserCards, rightDeckSeed);
        log.trace("[Session] randomSeed={}, leftDeckSeed={}, rightDeckSeed={}, sessionId={}",
                randomSeed, leftDeckSeed, rightDeckSeed, sessionId);
        // Both callbacks fire on the PingChecker scheduler thread. Swapping a bot in or out changes
        // what the next frame ticks, so the work is queued for the loop thread instead of applied here.
        this.pingChecker = new PingChecker(leftUserId, rightUserId,
                userId -> submitBotToggle(userId, "activateBot", WordOnlineLoop::activateBotForUser),
                userId -> submitBotToggle(userId, "deactivateBot", WordOnlineLoop::deactivateBotForUser)
        );
        this.sessionType = sessionType;
        this.scenarioId = scenarioId;
    }

    public SessionObject(String sessionId,
                         long leftUserId,
                         long rightUserId,
                         SimpMessagingTemplate template,
                         List<CardType> leftUserCards,
                         List<CardType> rightUserCards,
                         SessionType sessionType) {
        this(sessionId, leftUserId, rightUserId, template, leftUserCards, rightUserCards, sessionType, null);
    }

    public SessionObject(String sessionId,
                         long leftUserId,
                         long rightUserId,
                         SimpMessagingTemplate template,
                         List<CardType> leftUserCards,
                         List<CardType> rightUserCards) {
        this(sessionId, leftUserId, rightUserId, template, leftUserCards, rightUserCards, SessionType.PVP, null);
    }

    private void submitBotToggle(long userId, String actionName, BiConsumer<WordOnlineLoop, Long> toggle) {
        if (getUserSide(userId) == null) {
            return;
        }
        if (gameLoop instanceof WordOnlineLoop wordOnlineLoop) {
            wordOnlineLoop.getGameContext()
                    .submitAction(actionName, () -> toggle.accept(wordOnlineLoop, userId));
        }
    }

    // this method is used to send the frame information to the client
    public void sendFrameInfo(long userId, Object data) {
        // Skip sending frame info to bots (negative user IDs)
        if (userId < 0) {
            return;
        }
        template.convertAndSend(String.format("%s/%d", url, userId), data);
    }

    // this method is used to broadcast frame information to spectators (userId = 0)
    public void broadcastFrameInfo(Object data) {
        template.convertAndSend(String.format("%s/0", url), data);
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
