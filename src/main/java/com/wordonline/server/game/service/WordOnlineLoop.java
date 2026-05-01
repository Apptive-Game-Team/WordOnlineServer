package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.bot.BotAgent;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.system.BotAgentSystem;
import com.wordonline.server.game.service.system.ComponentUpdateSystem;
import com.wordonline.server.game.service.system.FeverTimeSystem;
import com.wordonline.server.game.service.system.GameObjectAddRemoteSystem;
import com.wordonline.server.game.service.system.GameObjectStateInitialSystem;
import com.wordonline.server.game.service.system.PhysicSystem;
import com.wordonline.server.game.service.system.SyncFrameDataSystem;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Scope("prototype")
@Service
public class WordOnlineLoop extends GameLoop {

    private final SyncFrameDataSystem frameDataSystem;
    private final BotAgentSystem botSystem;
    private final FeverTimeSystem feverTimeSystem;
    private final GameObjectStateInitialSystem gameObjectStateInitialSystem;
    private final ComponentUpdateSystem componentUpdateSystem;
    private final PhysicSystem physicSystem;
    private final GameObjectAddRemoteSystem gameObjectAddRemoveSystem;
    private final DatabaseMagicParser magicParser;

    private volatile BotAgent leftBotAgent;
    private volatile BotAgent rightBotAgent;

    public WordOnlineLoop(MmrService mmrService,
                          UserService userService, GameContext gameContext,
                          Parameters parameters, SyncFrameDataSystem frameDataSystem, BotAgentSystem botSystem,
            FeverTimeSystem feverTimeSystem,
                          GameObjectStateInitialSystem gameObjectStateInitialSystem,
                          ComponentUpdateSystem componentUpdateSystem, PhysicSystem physicSystem,
                          GameObjectAddRemoteSystem gameObjectAddRemoveSystem, DatabaseMagicParser magicParser) {
        super(mmrService, userService, gameContext, parameters);
        this.frameDataSystem = frameDataSystem;
        this.botSystem = botSystem;
        this.feverTimeSystem = feverTimeSystem;
        this.gameObjectStateInitialSystem = gameObjectStateInitialSystem;
        this.componentUpdateSystem = componentUpdateSystem;
        this.physicSystem = physicSystem;
        this.gameObjectAddRemoveSystem = gameObjectAddRemoveSystem;
        this.magicParser = magicParser;
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        gameContext.init(sessionObject, this);
        super.init(sessionObject, onTerminated);
        if(sessionObject.getSessionType() == SessionType.Practice) {
            initializeBotAgents(sessionObject);
        }
    }

    private void initializeBotAgents(SessionObject sessionObject) {
        if(sessionObject.isLeftBot()) {
            leftBotAgent = new BotAgent(sessionObject, magicParser, Master.LeftPlayer);
        }
        if(sessionObject.isRightBot()) {
            rightBotAgent = new BotAgent(sessionObject, magicParser, Master.RightPlayer);
        }
    }

    public synchronized void activateBotForUser(long userId) {
        Master side = sessionObject.getUserSide(userId);
        if (side == Master.LeftPlayer) {
            activateLeftBot();
        } else if (side == Master.RightPlayer) {
            activateRightBot();
        }
    }

    private void activateLeftBot() {
        if (leftBotAgent != null) {
            return;
        }
        leftBotAgent = new BotAgent(sessionObject, magicParser, Master.LeftPlayer);
        log.info("Activated bot control for disconnected user: side={}", Master.LeftPlayer);
    }

    private void activateRightBot() {
        if (rightBotAgent != null) {
            return;
        }
        rightBotAgent = new BotAgent(sessionObject, magicParser, Master.RightPlayer);
        log.info("Activated bot control for disconnected user: side={}", Master.RightPlayer);
    }

    public synchronized void deactivateBotForUser(long userId) {
        Master side = sessionObject.getUserSide(userId);
        if (side == Master.LeftPlayer) {
            deactivateLeftBot();
        } else if (side == Master.RightPlayer) {
            deactivateRightBot();
        }
    }

    private void deactivateLeftBot() {
        if (sessionObject.isLeftBot() || leftBotAgent == null) {
            return;
        }
        leftBotAgent = null;
        log.info("Deactivated bot control for reconnected user: side={}", Master.LeftPlayer);
    }

    private void deactivateRightBot() {
        if (sessionObject.isRightBot() || rightBotAgent == null) {
            return;
        }
        rightBotAgent = null;
        log.info("Deactivated bot control for reconnected user: side={}", Master.RightPlayer);
    }

    protected void update() {
        // Initial DTOs
        frameDataSystem.earlyUpdate(gameContext);

        feverTimeSystem.update(gameContext);

        // bot tick (practice bots and pve enemy bots are both handled inside BotAgentSystem)
        botSystem.update(gameContext);

        beforeResultCheck();

        if (gameContext.getGameTimer().isEnd()) {
            gameContext.getResultChecker().setEnd();
        }

        // Check for game over
        if (gameContext.getResultChecker().checkResult()) {
            handleGameEnd();
        }

        gameObjectStateInitialSystem.update(gameContext);

        // Run GameObject's Updates
        componentUpdateSystem.update(gameContext);

        physicSystem.update(gameContext);

        gameObjectAddRemoveSystem.update(gameContext);

        buildSnapshot();

        frameDataSystem.lateUpdate(gameContext);
    }

    protected void beforeResultCheck() {
        // hook for specialized loops (e.g. PVE)
    }
}
