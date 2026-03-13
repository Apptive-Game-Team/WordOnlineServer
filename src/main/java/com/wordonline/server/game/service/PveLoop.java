package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import com.wordonline.server.game.service.pve.PveScenarioInstaller;
import com.wordonline.server.game.service.pve.PveScenarioRegistry;
import com.wordonline.server.game.service.system.PveScriptSystem;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Scope("prototype")
public class PveLoop extends WordOnlineLoop {

    private static final String DEFAULT_STAGE_ID = "1-3";
    private static final Pattern STAGE_ID_PATTERN = Pattern.compile("(\\d+-\\d+)");

    private final PveScenarioRegistry pveScenarioRegistry;
    private final PveScenarioInstaller pveScenarioInstaller;
    private final PveScriptSystem pveScriptSystem;

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
                   com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser magicParser,
                   PveScenarioRegistry pveScenarioRegistry,
                   PveScenarioInstaller pveScenarioInstaller,
                   PveScriptSystem pveScriptSystem) {
        super(mmrService, userService, gameContext, parameters, frameDataSystem, botSystem, feverTimeSystem,
                gameObjectStateInitialSystem, componentUpdateSystem, physicSystem, gameObjectAddRemoveSystem, magicParser);
        this.pveScenarioRegistry = pveScenarioRegistry;
        this.pveScenarioInstaller = pveScenarioInstaller;
        this.pveScriptSystem = pveScriptSystem;
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        gameContext.init(sessionObject, this);
        initializeLoop(sessionObject, onTerminated, false);

        PveResultChecker resultChecker = new PveResultChecker(sessionObject);
        gameContext.setResultChecker(resultChecker);

        // PveLoop should always setup a PVE scenario regardless of SessionType guard.
        setupPveScenario(sessionObject, resultChecker);
    }

    @Override
    protected void beforeResultCheck() {
        if (sessionObject.getSessionType() != SessionType.PVE) {
            return;
        }

        pveScriptSystem.update(gameContext);
    }

    private void setupPveScenario(SessionObject sessionObject, PveResultChecker resultChecker) {
        String stageId = resolveStageId(sessionObject.getSessionId());
        var scenario = pveScenarioRegistry.getScenario(stageId);

        pveScenarioInstaller.install(stageId, scenario.installers(), gameContext);
        pveScriptSystem.setScenario(scenario);
        pveScriptSystem.setRuntime(pveScenarioInstaller.getRuntime());

        List<Integer> objectiveIds = scenario.objectiveInstallerIds().stream()
                .map(installerId -> pveScenarioInstaller.getRuntime() == null
                        ? -1
                        : pveScenarioInstaller.getRuntime().getInstalledObjectId(installerId))
                .toList();
        resultChecker.setObjectiveIds(objectiveIds);
    }

    private String resolveStageId(String sessionId) {
        if (sessionId == null) {
            return DEFAULT_STAGE_ID;
        }

        Matcher matcher = STAGE_ID_PATTERN.matcher(sessionId);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return DEFAULT_STAGE_ID;
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
