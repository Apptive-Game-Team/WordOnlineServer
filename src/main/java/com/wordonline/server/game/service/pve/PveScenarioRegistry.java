package com.wordonline.server.game.service.pve;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.pve.PveDialogue;
import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveTrigger;
import com.wordonline.server.game.domain.pve.PveTriggerType;
import com.wordonline.server.game.domain.pve.PveWave;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PveScenarioRegistry {

    private final Map<String, PveScenario> scenarios = Map.of(
            "1-1",
            new PveScenario(
                    "1-1",
                    List.of(
                            new PveWave(0, List.of(
                                    new PveWave.PveSpawn(PrefabType.FireSlime, 3)
                            )),
                            new PveWave(40, List.of(
                                    new PveWave.PveSpawn(PrefabType.WaterSlime, 4)
                            )),
                            new PveWave(60, List.of(
                                    new PveWave.PveSpawn(PrefabType.RockSlime, 5)
                            ))
                    ),
                    List.of(
                            new PveTrigger(
                                    "intro",
                                    PveTriggerType.FrameNumGte,
                                    10,
                                    new PveDialogue("pve_1_1_intro", List.of("Stage 1-1", "Defeat all enemies!"))
                            ),
                            new PveTrigger(
                                    "lastWave",
                                    PveTriggerType.WaveIndexEnter,
                                    2,
                                    new PveDialogue("pve_1_1_last_wave", List.of("Here they come!"))
                            )
                    ),
                    List.of(PrefabType.FireSlime, PrefabType.WaterSlime, PrefabType.RockSlime)
            )
    );

    public PveScenario getScenario(String stageId) {
        PveScenario scenario = scenarios.get(stageId);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown stageId: " + stageId);
        }
        return scenario;
    }
}
