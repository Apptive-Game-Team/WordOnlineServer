package com.wordonline.server.game.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.bot.BotAgent;
import com.wordonline.server.game.service.system.ComponentUpdateSystem;
import com.wordonline.server.game.service.system.FrameDataSystem;
import com.wordonline.server.game.service.system.GameObjectAddRemoteSystem;
import com.wordonline.server.game.service.system.GameObjectStateInitialSystem;
import com.wordonline.server.game.service.system.GameSystem;
import com.wordonline.server.game.service.system.PhysicSystem;

@Scope("prototype")
@Service
public class WordOnlineLoop extends GameLoop {

    private BotAgent botAgent;

    private final FrameDataSystem frameDataSystem = new FrameDataSystem();
    private final GameSystem gameObjectStateInitialSystem = new GameObjectStateInitialSystem();
    private final GameSystem componentUpdateSystem = new ComponentUpdateSystem();
    private final GameSystem physicSystem = new PhysicSystem();
    private final GameSystem gameObjectAddRemoveSystem = new GameObjectAddRemoteSystem();

    public WordOnlineLoop(MmrService mmrService,
            UserService userService, GameContext gameContext,
            Parameters parameters) {
        super(mmrService, userService, gameContext, parameters);
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        super.init(sessionObject, onTerminated);
        if(sessionObject.getSessionType() == SessionType.Practice)
        {
            botAgent = new BotAgent(sessionObject);
        }
    }

    protected void update() {
        // Initial DTOs
        frameDataSystem.earlyUpdate(gameContext);

        //bot tick
        if(botAgent != null)
        {
            botAgent.onTick(frameDataSystem.getRightFrameInfoDto());
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

        frameDataSystem.lateUpdate(gameContext);

        buildSnapshot();
    }
}
