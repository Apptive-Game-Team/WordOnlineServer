package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.pve.PveInstallObject;
import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveScenarioEvent;
import com.wordonline.server.game.domain.pve.PveTriggerType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.dto.lockstep.InitialObjectDto;
import com.wordonline.server.game.dto.lockstep.PveEventDto;
import com.wordonline.server.game.service.pve.PveScenarioRegistry;
import com.wordonline.server.game.service.system.BotAgentSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lockstep loop for PVE sessions.
 *
 * The server does NOT simulate PVE. It:
 *   1. Loads the scenario from DB.
 *   2. Sends initialObjects + scenarioEvents in sessionStart so the client
 *      SimWorld can spawn objects and fire dialogue autonomously.
 *   3. Relays the single human player's inputs each frame (no right player).
 *   4. Terminates when the client sends a result via ResultController.
 */
@Slf4j
@Scope("prototype")
@Service
public class PveLockstepLoop extends InputRelayLoop {

    private final PveScenarioRegistry scenarioRegistry;

    public PveLockstepLoop(MmrService mmrService,
                           UserService userService,
                           GameContext gameContext,
                           Parameters parameters,
                           DatabaseMagicParser magicParser,
                           BotAgentSystem botSystem,
                           PveScenarioRegistry scenarioRegistry) {
        super(mmrService, userService, gameContext, parameters, magicParser, botSystem);
        this.scenarioRegistry = scenarioRegistry;
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        gameContext.init(sessionObject, this);
        super.initializeLoop(sessionObject, onTerminated);

        this.rngSeed = java.util.concurrent.ThreadLocalRandom.current().nextLong();

        Long scenarioId = sessionObject.getScenarioId();
        List<InitialObjectDto> initialObjects = null;
        List<PveEventDto> scenarioEvents = null;

        if (scenarioId != null) {
            PveScenario scenario = scenarioRegistry.getScenario(scenarioId);
            initialObjects = toInitialObjectDtos(scenario.installers());
            scenarioEvents = toScenarioEventDtos(scenario.events());
            log.info("[PVE] Loaded scenario {}: {} objects, {} events",
                    scenarioId, initialObjects.size(), scenarioEvents.size());
        } else {
            log.warn("[PVE] Session {} has no scenarioId — starting with empty world", sessionObject.getSessionId());
        }

        sendSessionStart(sessionObject, initialObjects, scenarioEvents);
    }

    private static List<InitialObjectDto> toInitialObjectDtos(List<PveInstallObject> installers) {
        if (installers == null) return List.of();
        return installers.stream()
                .map(obj -> new InitialObjectDto(
                        obj.installerId(),
                        obj.prefabType().name(),
                        obj.master().name(),
                        obj.position().getX(),
                        obj.position().getY(),
                        obj.position().getZ()
                ))
                .toList();
    }

    private static List<PveEventDto> toScenarioEventDtos(List<PveScenarioEvent> events) {
        if (events == null) return List.of();
        return events.stream()
                .filter(e -> e.type() == PveTriggerType.FrameNumGte)
                .map(e -> new PveEventDto(
                        e.value(),
                        e.speakerInstallerId(),
                        e.key(),
                        e.lines()
                ))
                .toList();
    }
}
