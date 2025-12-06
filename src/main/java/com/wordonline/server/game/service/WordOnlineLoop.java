package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.bot.BotAgent;
import com.wordonline.server.game.service.system.BotAgentSystem;
import com.wordonline.server.game.service.system.ComponentUpdateSystem;
import com.wordonline.server.game.service.system.GameObjectAddRemoteSystem;
import com.wordonline.server.game.service.system.GameObjectStateInitialSystem;
import com.wordonline.server.game.service.system.PhysicSystem;
import com.wordonline.server.game.service.system.SyncFrameDataSystem;

import lombok.Getter;

@Getter
@Scope("prototype")
@Service
public class WordOnlineLoop extends GameLoop {

    private final SyncFrameDataSystem frameDataSystem;
    private final BotAgentSystem botSystem;
    private final GameObjectStateInitialSystem gameObjectStateInitialSystem;
    private final ComponentUpdateSystem componentUpdateSystem;
    private final PhysicSystem physicSystem;
    private final GameObjectAddRemoteSystem gameObjectAddRemoveSystem;
    private final DatabaseMagicParser magicParser;

    private BotAgent botAgent;

    public WordOnlineLoop(MmrService mmrService,
                          UserService userService, GameContext gameContext,
                          Parameters parameters, SyncFrameDataSystem frameDataSystem, BotAgentSystem botSystem,
                          GameObjectStateInitialSystem gameObjectStateInitialSystem,
                          ComponentUpdateSystem componentUpdateSystem, PhysicSystem physicSystem,
                          GameObjectAddRemoteSystem gameObjectAddRemoveSystem, DatabaseMagicParser magicParser) {
        super(mmrService, userService, gameContext, parameters);
        this.frameDataSystem = frameDataSystem;
        this.botSystem = botSystem;
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
        if(sessionObject.getSessionType() == SessionType.Practice)
        {
            botAgent = new BotAgent(sessionObject, magicParser);
        }
    }

    protected void update() {
        // Initial DTOs
        frameDataSystem.earlyUpdate(gameContext);

        //bot tick
        if (botAgent != null)
            botSystem.update(gameContext);

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
}
