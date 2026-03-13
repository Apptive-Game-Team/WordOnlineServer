package com.wordonline.server.game.service.system;

import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveScenarioEvent;
import com.wordonline.server.game.dto.pve.PveScriptEventDto;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.pve.PveScenarioInstaller;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("prototype")
public class PveScriptSystem implements GameSystem {

    @Setter
    private PveScenario scenario;

    @Setter
    private PveScenarioInstaller.RuntimeState runtime;

    private final Set<String> fired = new HashSet<>();

    @Override
    public void update(GameContext gameContext) {
        if (scenario == null) {
            return;
        }

        for (PveScenarioEvent eventSpec : scenario.events()) {
            if (fired.contains(eventSpec.id())) {
                continue;
            }
            if (isSatisfied(eventSpec, gameContext)) {
                fired.add(eventSpec.id());
                int speakerObjectId = runtime == null ? -1 : runtime.getInstalledObjectId(eventSpec.speakerInstallerId());
                var event = new PveScriptEventDto(eventSpec.key(), speakerObjectId, eventSpec.lines());
                long leftId = gameContext.getSessionObject().getLeftUserId();
                long rightId = gameContext.getSessionObject().getRightUserId();
                gameContext.getSessionObject().sendFrameInfo(leftId, event);
                gameContext.getSessionObject().sendFrameInfo(rightId, event);
            }
        }
    }

    private boolean isSatisfied(PveScenarioEvent eventSpec, GameContext gameContext) {
        return gameContext.getFrameNum() >= eventSpec.value();
    }
}
