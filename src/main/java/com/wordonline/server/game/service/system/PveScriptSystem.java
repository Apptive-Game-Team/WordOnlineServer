package com.wordonline.server.game.service.system;

import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveTrigger;
import com.wordonline.server.game.domain.pve.PveTriggerType;
import com.wordonline.server.game.dto.Master;
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

        for (PveTrigger trigger : scenario.triggers()) {
            if (fired.contains(trigger.id())) {
                continue;
            }
            if (isSatisfied(trigger, gameContext)) {
                fired.add(trigger.id());
                var event = new PveScriptEventDto(trigger.dialogue().key(), trigger.dialogue().lines());
                long leftId = gameContext.getSessionObject().getLeftUserId();
                long rightId = gameContext.getSessionObject().getRightUserId();
                gameContext.getSessionObject().sendFrameInfo(leftId, event);
                gameContext.getSessionObject().sendFrameInfo(rightId, event);
            }
        }
    }

    private boolean isSatisfied(PveTrigger trigger, GameContext gameContext) {
        PveTriggerType type = trigger.type();
        int v = trigger.value();

        return switch (type) {
            case FrameNumGte -> gameContext.getFrameNum() >= v;
            case WaveIndexEnter -> runtime != null && runtime.getWaveIndex() == v;
            case EnemyRemainingLte -> {
                if (runtime == null) {
                    yield false;
                }
                long remaining = gameContext.getGameObjects().stream()
                        .filter(o -> o.isActive() && o.getMaster() == Master.RightPlayer)
                        .filter(o -> runtime.getEnemyPrefabTypes().contains(o.getType()))
                        .count();
                yield remaining <= v;
            }
        };
    }
}
