package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.pvebot.PveEnemyBot;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class PveLoop extends WordOnlineLoop {

    private PveEnemyBot leftPveEnemyBot;
    private PveEnemyBot rightPveEnemyBot;

    public PveLoop(MmrService mmrService,
                   UserService userService,
                   GameContext gameContext,
                   com.wordonline.server.game.domain.Parameters parameters,
                   com.wordonline.server.game.service.system.SyncFrameDataSystem frameDataSystem,
                   com.wordonline.server.game.service.system.BotAgentSystem botSystem,
                   com.wordonline.server.game.service.system.FeverTimeSystem feverTimeSystem,
                   com.wordonline.server.game.service.system.GameObjectStateInitialSystem gameObjectStateInitialSystem,
                   com.wordonline.server.game.service.system.ComponentUpdateSystem componentUpdateSystem,
                   com.wordonline.server.game.service.system.PhysicSystem physicSystem,
                   com.wordonline.server.game.service.system.GameObjectAddRemoteSystem gameObjectAddRemoveSystem,
                   com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser magicParser) {
        super(mmrService, userService, gameContext, parameters, frameDataSystem, botSystem, feverTimeSystem,
                gameObjectStateInitialSystem, componentUpdateSystem, physicSystem, gameObjectAddRemoveSystem, magicParser);
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        gameContext.init(sessionObject, this);
        super.init(sessionObject, onTerminated);
        if (sessionObject.getSessionType() == SessionType.PVE) {
            initializePveEnemyBots(sessionObject);
        }
        gameContext.setResultChecker(new PveResultChecker(sessionObject));
    }

    private void initializePveEnemyBots(SessionObject sessionObject) {
        if (sessionObject.isLeftBot()) {
            leftPveEnemyBot = new PveEnemyBot(sessionObject, getMagicParser(), Master.LeftPlayer);
        }
        if (sessionObject.isRightBot()) {
            rightPveEnemyBot = new PveEnemyBot(sessionObject, getMagicParser(), Master.RightPlayer);
        }
    }

    public PveEnemyBot getLeftPveEnemyBot() {
        return leftPveEnemyBot;
    }

    public PveEnemyBot getRightPveEnemyBot() {
        return rightPveEnemyBot;
    }

    @Override
    protected void handleGameEnd() {
        long leftId = sessionObject.getLeftUserId();
        long rightId = sessionObject.getRightUserId();

        ResultMmrDto mmrDto = new ResultMmrDto((short) 0, (short) 0, (short) 0, (short) 0);
        gameContext.getResultChecker().broadcastResult(mmrDto);

        if (leftId >= 0) {
            getUserService().markOnline(leftId);
        }
        if (rightId >= 0) {
            getUserService().markOnline(rightId);
        }
        close();
    }
}
